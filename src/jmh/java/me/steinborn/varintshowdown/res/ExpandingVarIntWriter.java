package me.steinborn.varintshowdown.res;

import io.netty.buffer.ByteBuf;

public class ExpandingVarIntWriter implements VarIntWriter {

  @Override
  public void write(ByteBuf buf, int value) {
    if ((value & 0x7F) == value) {
      buf.writeByte(value);
    } else {
      long expand = Long.expand(value & 0xFFFFFFFFL, 0b111101111111011111110111111101111111L);
      int lower = (int) expand;
      switch (Integer.numberOfLeadingZeros(value)) {
        case 24, 23, 22, 21, 20, 19, 18 -> buf.writeShortLE(lower | 0x80);
        case 17, 16, 15, 14, 13, 12, 11 -> buf.writeMediumLE(lower | 0x8080);
        case 10,  9,  8,  7,  6,  5,  4 -> buf.writeIntLE(lower | 0x808080);
        default -> {
          buf.writeIntLE(lower | 0x80808080);
          buf.writeByte((int) (expand >> 32L));
        }
      }
    }
  }
}
