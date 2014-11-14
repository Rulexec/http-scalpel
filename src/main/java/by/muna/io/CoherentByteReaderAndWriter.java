package by.muna.io;

public class CoherentByteReaderAndWriter {
    private boolean ended = false;

    public CoherentByteReaderAndWriter(){}

    private IByteReader reader = new IByteReader() {
        @Override
        public int read(byte[] buffer, int offset, int length) {
            return 0;
        }

        @Override
        public void end() {
            CoherentByteReaderAndWriter.this.ended = true;
        }

        @Override
        public boolean isEnded() {
            return CoherentByteReaderAndWriter.this.ended;
        }
    };
    private IByteWriter writer = new IByteWriter() {


        @Override
        public void end() {
            CoherentByteReaderAndWriter.this.ended = true;
        }

        @Override
        public boolean isEnded() {
            return CoherentByteReaderAndWriter.this.ended;
        }
    };

    public IByteReader getByteReader() { return this.reader; }
    public IByteWriter getByteWriter() { return this.writer; }
}
