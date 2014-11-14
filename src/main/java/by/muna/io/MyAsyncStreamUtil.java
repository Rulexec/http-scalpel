package by.muna.io;

import java.util.function.Supplier;

public class MyAsyncStreamUtil {
    public static void pipeToStreams(
        IAsyncByteInputStream input, IAsyncByteOutputStream outputStream, Supplier<IAsyncByteOutputStream> next,
        boolean closeOutputIfInputClosed)
    {
        throw new RuntimeException("Not implemented yet");
        /*class Container {
            IByteReader reader;
            IByteWriter writer;

            IAsyncByteOutputStream output = outputStream;
        }

        Container c = new Container();

        input.onCanRead(reader -> {
            if (reader.isEnded()) {
                synchronized (c) { c.reader = null; }
                c.output.requestWriting(false);
                return false;
            }

            c.reader = reader;
            synchronized (c) {
                if (c.writer != null) {
                    reader.read(c.writer);
                }
            }

            c.output.requestWriting();

            return false;
        });
        Consumer<IAsyncByteOutputStream> setCanWriteListener = out ->
            out.onCanWrite(writer -> {
                if (writer.isEnded()) {
                    synchronized (c) { c.writer = null; }
                    input.requestReading(false);
                    return false;
                }

                c.writer = writer;
                synchronized (c) {
                    if (c.reader != null) {
                        writer.write(c.reader);
                    }
                }

                input.requestReading();

                return false;
            });
        setCanWriteListener.accept(outputStream);

        if (closeOutputIfInputClosed) input.onEnd().run(x -> c.output.end().run(y -> {}));

        class Lambda {
            public void setEndListener(IAsyncByteOutputStream output) {
                output.onEnd().run(x -> {
                    c.writer = null;
                    input.requestReading(false);

                    IAsyncByteOutputStream newOutput = next.get();
                    if (newOutput != null) {
                        c.output = newOutput;
                        setCanWriteListener.accept(newOutput);
                        Lambda.this.setEndListener(newOutput);

                        input.requestReading();
                        newOutput.requestWriting();
                    }
                });
            }
        }
        new Lambda().setEndListener(outputStream);

        input.requestReading();
        outputStream.requestWriting();*/
    }

    public static void pipeToStreams(IAsyncByteInputStream input, Supplier<IAsyncByteOutputStream> next) {
        MyAsyncStreamUtil.pipeToStreams(input, next.get(), next, true);
    }
}
