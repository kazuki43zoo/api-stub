package com.kazuki43zoo.apistub.ui.component.message;

public abstract class AbstractMessage {

  private MessageCode code = MessageCode.UNDEFINED;
  private MessageArgs args = MessageArgs.EMPTY;

  public MessageCode getCode() {
    return code;
  }

  public void setCode(MessageCode code) {
    this.code = code;
  }

  public MessageArgs getArgs() {
    return args;
  }

  public void setArgs(MessageArgs args) {
    this.args = args;
  }

  public static class MessageBuilder<T extends AbstractMessage> {
    private final T message;

    public MessageBuilder(T message) {
      this.message = message;
    }

    public MessageBuilder<T> code(MessageCode code) {
      this.message.setCode(code);
      return this;
    }

    public MessageBuilder<T> args(MessageArgs args) {
      this.message.setArgs(args);
      return this;
    }

    public T build() {
      return message;
    }
  }

}
