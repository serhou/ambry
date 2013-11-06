package com.github.ambry.coordinator;

import com.github.ambry.messageformat.BlobProperties;
import com.github.ambry.messageformat.DataCorruptException;
import com.github.ambry.messageformat.MessageFormat;
import com.github.ambry.messageformat.MessageFormatFlags;
import com.github.ambry.shared.*;
import com.github.ambry.utils.ByteBufferInputStream;

import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class AmbryCoordinator implements Coordinator {

  @Override
  public String putBlob(BlobProperties blobProperties, ByteBuffer userMetadata, InputStream blob) {
    try {
      // put blob
      PutRequest putRequest = new PutRequest((short)1, 1, 1, "client1", "id1", userMetadata, blob, blobProperties);
      BlockingChannel channel = new BlockingChannel("localhost", 6667, 10000, 10000, 10000);
      channel.connect();
      channel.send(putRequest);
      InputStream putResponseStream = channel.receive();
      PutResponse response = PutResponse.readFrom(new DataInputStream(putResponseStream));
      return "id1";
    }
    catch (Exception e) {
      // need to retry on errors by choosing another partition. If it still fails, throw AmbryException
    }
    return null; // this will not happen in the actual implementation
  }

  @Override
  public void deleteBlob(String id) throws BlobNotFoundException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public InputStream getBlob(String blobId) throws BlobNotFoundException {
    // get blob
    try {
      GetResponse response = doGetResponse(blobId, MessageFormatFlags.Data);
      InputStream data = MessageFormat.deserializeData(response.getInputStream());
      return data;
    }
    catch (Exception e) {
      // need to retry on errors by choosing another partition. If it still fails, throw AmbryException
    }
    return null; // this will never happen once Ambry Exception is defined
  }

  @Override
  public ByteBuffer getUserMetadata(String blobId) throws BlobNotFoundException {
    try {
      GetResponse response = doGetResponse(blobId, MessageFormatFlags.UserMetadata);
      ByteBuffer userMetadata = MessageFormat.deserializeMetadata(response.getInputStream());
      return userMetadata;
    }
    catch (Exception e) {
      // need to retry on errors by choosing another partition. If it still fails, throw AmbryException
    }
    return null; // this will never happen once Ambry Exception is defined
  }

  @Override
  public BlobProperties getBlobProperties(String blobId) throws BlobNotFoundException {
    try {
      GetResponse response = doGetResponse(blobId, MessageFormatFlags.BlobProperties);
      BlobProperties properties = MessageFormat.deserializeBlobProperties(response.getInputStream());
      return properties;
    }
    catch (Exception e) {
      // need to retry on errors by choosing another partition. If it still fails, throw AmbryException
    }
    return null; // this will never happen once Ambry Exception is defined
  }

  private GetResponse doGetResponse(String blobId, MessageFormatFlags flag) {
    try {
      ArrayList<BlobId> ids = new ArrayList<BlobId>();
      ids.add(new BlobId(blobId));
      GetRequest getRequest = new GetRequest((short)0, 1, MessageFormatFlags.BlobProperties, ids);
      BlockingChannel channel = new BlockingChannel("localhost", 6667, 10000, 10000, 10000);
      channel.send(getRequest);
      InputStream stream = channel.receive();
      GetResponse response = GetResponse.readFrom(new DataInputStream(stream));
      return response;
    }
    catch (Exception e) {
      // need to retry on errors by choosing another partition. If it still fails, throw AmbryException
    }
    return null; // this will never happen once Ambry Exception is defined
  }
}