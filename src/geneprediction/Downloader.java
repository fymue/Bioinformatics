package geneprediction;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class Downloader
{

  public static DownloadStatus download(String saveAsFile, String urlOfFile)
  {
    InputStream httpIn = null;
    OutputStream fileOutput = null;
    OutputStream bufferedOut = null;
    try
    {
      // check the http connection before we do anything to the fs
      httpIn = new BufferedInputStream(new URL(urlOfFile).openStream());
      // prep saving the file
      fileOutput = new FileOutputStream(saveAsFile);
      bufferedOut = new BufferedOutputStream(fileOutput, 1024);
      byte data[] = new byte[1024];
      boolean fileComplete = false;
      int count = 0;
      while (!fileComplete)
      {
        count = httpIn.read(data, 0, 1024);
        if (count <= 0)
        {
          fileComplete = true;
        } 
        else
        {
          bufferedOut.write(data, 0, count);
        }
      }
    } 
    catch (MalformedURLException e)
    {
      return DownloadStatus.MalformedUrl;
    }
    catch (IOException e)
    {
      return DownloadStatus.IoException;
    }
    finally
    {
      try
      {
        bufferedOut.close();
        fileOutput.close();
        httpIn.close();
      }
      catch (IOException e)
      {
        return DownloadStatus.UnableToCloseOutputStream;
      }
    }
    return DownloadStatus.Success;
  }
}
