package ru.daniilazarnov;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.daniilazarnov.commands.State;
import ru.daniilazarnov.commands.UpLoadFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    private UpLoadFile upLoadFile = new UpLoadFile();
    public  final byte DOWNLOAD = (byte) 25;
    public final  byte UPLOAD = (byte) 24;
    private State currentState = State.IDLE;
    private int nextLength;
    private String fileName;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private FileOutputStream fos;



    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            //Проверка ключа операции
            if (currentState == State.IDLE) {
                byte readed = buf.readByte();
                if (readed == DOWNLOAD) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("STATE: Start file receiving");
                } else {
                    System.out.println("ERROR: Invalid first byte - " + readed);
                }
                if(readed == UPLOAD){
//                    try {
                        //C:\Users\Stas\Desktop\Java.NET\java-net-course\project\client\src\main\java\ru\daniilazarnov\demo.txt
//                        upLoadFile.sendFile(pathFile, Network.getInstance().getCurrentChannel(), future -> {
//                            if (!future.isSuccess()) {
//                                future.cause().printStackTrace();
//                                                Network.getInstance().stop();
//                            }
//                            if (future.isSuccess()) {
//                                System.out.println("Файл успешно передан");
//                                                Network.getInstance().stop();
//                            }
//                        });
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    System.out.println("Команда читается сервером");

                    break;
                }
            }

            //Длинна названия файла
            if (currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= (Integer.SIZE / Byte.SIZE)) {
                    System.out.println("STATE: Get filename length " + nextLength);
                    nextLength = buf.readInt();
                    currentState = State.NAME;
                }
            }

            //Имя файла
            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fName = new byte[nextLength];
                    buf.readBytes(fName);
                    System.out.println("STATE: Filename received - _" + new String(fName, "UTF-8"));
                    // out = new BufferedOutputStream(new FileOutputStream("_" + new String(fName)));
                    currentState = State.FILE_LENGTH;
                    fileName = new String(fName, "UTF-8");
                    fos = new FileOutputStream("C:\\Users\\Stas\\Desktop\\Java.NET\\java-net-course\\project\\server\\src\\main\\java\\ru\\daniilazarnov\\" + fileName);
                    // fos = new FileOutputStream(f);
                }

            }
            //Длинна файла
            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= (Long.SIZE / Byte.SIZE)) {
                    fileLength = buf.readLong();
                    System.out.println("STATE: File length received - " + fileLength);
                    currentState = State.FILE;
                }
            }
            //Содержание файла
            if (currentState == State.FILE) {
                int count;
                while (buf.readableBytes() > 0) {
                    //out.write(buf.readByte());
                    byte[] b = new byte[(int) fileLength];
                    buf.readBytes(b);

                    fos.write(b, 0, (int) fileLength);
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.IDLE;
                        System.out.println("File received");
                        fos.close();
                        out.close();
                        break;
                    }
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}