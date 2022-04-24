package geneprediction;

import java.util.Scanner;
import java.util.BitSet;
import java.util.zip.GZIPInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.Buffer;
import java.lang.Exception;
import geneprediction.Downloader;
import geneprediction.DownloadStatus;


public class SeqDataParser
{
    //download and parse the sequence data required to build and use the HMM
    BitSet cdsSeq;
    String trainingSeq;
    String sampleSeq;
    int totalGenes = 0;
    String[][] dlFileNames = {{"TrainingOrg_genome.txt.gz", "TrainingOrg_features.txt.gz"}, {"SampleOrg_genome.txt.gz"}};

    public SeqDataParser(String trainingOrgId, String sampleOrgId, String saveDir)
    {
        fetchSeqData(trainingOrgId, sampleOrgId, saveDir, dlFileNames);
        this.trainingSeq = getSeq(saveDir + dlFileNames[0][0]);
        this.sampleSeq = getSeq(saveDir + dlFileNames[1][0]);
        this.cdsSeq = createCDSSeq(saveDir + dlFileNames[0][1]);
        this.totalGenes = totalGenes;
    }

    private void fetchSeqData(String trainingOrgId, String sampleOrgId, String saveDir, String[][] dlFileNames)
    {
        //download required files from NCBI FTP server

        String[] orgs = {trainingOrgId, sampleOrgId};
        String[][] fileNames = {{"_genomic.fna.gz ", "_feature_table.txt.gz"}, {"_genomic.fna.gz"}};

        for (int i=0; i<orgs.length; i++)
        {
            String org = orgs[i];
            String dlLink = "https://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/";
            String orgNumberId = org.substring(4, 13);
            for (int o=0; o<orgNumberId.length()-3+1; o+=3) dlLink += orgNumberId.substring(o, o+3) + "/";
            dlLink += orgs[i] + "/" + orgs[i];

            for (int j=0; j<fileNames[i].length; j++) Downloader.download(saveDir + dlFileNames[i][j], dlLink + fileNames[i][j]);
        }

    }

    private void decompressGzip(String sourceFile, String targetFile)
    {
        //decompress the downloaded NCBI gzip-Files before using them
        
        try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(sourceFile));
             FileOutputStream fos = new FileOutputStream(targetFile))
        {
            // copy GZIPInputStream to FileOutputStream
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) fos.write(buffer, 0, len);
        }
        catch (IOException ex)
        {
            System.out.println(ex);
        }
    }

    private BitSet createCDSSeq(String trainingSeqFeaturesFile)
    {   
        /*create the mapping sequence to the trainin sequence showing which regions are (non-)coding.
          Bases that are part of a coding region are represented by "true" bits in a BitSet object,
          bases of non-coding regions are "false" bits.*/

        int prevStart = 0;
        int startCDS = 0;
        int endCDS = 0;
        int totalSeqLength = this.trainingSeq.length();
        String decompressedFeaturesFile = trainingSeqFeaturesFile.replace(".gz", "");

        BitSet cdsSeq = new BitSet(totalSeqLength); //create BitSet with the same length as the training sequence (1 bit per base)
        
        //decompress the gzip file
        decompressGzip(trainingSeqFeaturesFile, decompressedFeaturesFile);

        try
        {
            BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(decompressedFeaturesFile)));
            String l;

            //read the sequence features file (contains positions of every gene etc.)
            while ((l = fin.readLine()) != null)
            {
                if (l.isBlank() || l.startsWith("#") || l.startsWith("gene")) continue;
                
                totalGenes++;

                String[] lineContent = l.split("\t");
                startCDS = Integer.parseInt(lineContent[7]); //start position of the current gene
                endCDS = Integer.parseInt(lineContent[8]); //end position of the current gene
                
                cdsSeq.set(startCDS, endCDS+1); //set bits between start and end to "true"
            }
            
            fin.close();
        }
        catch (Exception ex)
        {
            System.out.println(ex);
        }
        return cdsSeq;
    }

    private String getSeq(String seqFile)
    {
        //read the sequence fasta files to a string
        String seq = "";
        String decompressedSeqFile = seqFile.replace(".gz", "");

        //decompress the gzip-file
        decompressGzip(seqFile, decompressedSeqFile);

        try
        {
            //read the sequence to a string
            Path file = Path.of(decompressedSeqFile);
            System.out.println(file);
            seq = Files.readString(file);
            seq = seq.substring(seq.indexOf("\n")).replaceAll("\n", "");
        }
        catch (Exception ex)
        {
            System.out.println(ex);
        }
                
        return seq;
    }
}
