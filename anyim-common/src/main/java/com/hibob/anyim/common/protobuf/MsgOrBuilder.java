// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: msg.proto

// Protobuf Java Version: 4.26.1
package com.hibob.anyim.common.protobuf;

public interface MsgOrBuilder extends
    // @@protoc_insertion_point(interface_extends:com.hibob.anyim.common.protobuf.Msg)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.com.hibob.anyim.common.protobuf.Header header = 1;</code>
   * @return Whether the header field is set.
   */
  boolean hasHeader();
  /**
   * <code>.com.hibob.anyim.common.protobuf.Header header = 1;</code>
   * @return The header.
   */
  com.hibob.anyim.common.protobuf.Header getHeader();
  /**
   * <code>.com.hibob.anyim.common.protobuf.Header header = 1;</code>
   */
  com.hibob.anyim.common.protobuf.HeaderOrBuilder getHeaderOrBuilder();

  /**
   * <code>.com.hibob.anyim.common.protobuf.Body body = 2;</code>
   * @return Whether the body field is set.
   */
  boolean hasBody();
  /**
   * <code>.com.hibob.anyim.common.protobuf.Body body = 2;</code>
   * @return The body.
   */
  com.hibob.anyim.common.protobuf.Body getBody();
  /**
   * <code>.com.hibob.anyim.common.protobuf.Body body = 2;</code>
   */
  com.hibob.anyim.common.protobuf.BodyOrBuilder getBodyOrBuilder();

  /**
   * <code>optional .com.hibob.anyim.common.protobuf.Extension extension = 99;</code>
   * @return Whether the extension field is set.
   */
  boolean hasExtension();
  /**
   * <code>optional .com.hibob.anyim.common.protobuf.Extension extension = 99;</code>
   * @return The extension.
   */
  com.hibob.anyim.common.protobuf.Extension getExtension();
  /**
   * <code>optional .com.hibob.anyim.common.protobuf.Extension extension = 99;</code>
   */
  com.hibob.anyim.common.protobuf.ExtensionOrBuilder getExtensionOrBuilder();
}