package kz.logisto.lguserservice.web.socket.dto;

public enum ChatMessageType {

  // Client -> Server
  SEND_MESSAGE,
  CLOSE_CHAT,
  LIST_CHATS,
  LOAD_HISTORY,

  // Server -> Client
  CHAT_CREATED,
  NEW_MESSAGE,
  CHAT_CLOSED,
  CHAT_LIST,
  CHAT_HISTORY,
  ERROR
}
