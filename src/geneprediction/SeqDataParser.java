package geneprediction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import geneprediction.Downloader;
import geneprediction.DownloadStatus;

public class SeqDataParser
{
    String cdsSeq;
    String trainingSeq;
    String sampleSeq;
    int totalGenes = 0;
    String[][] dlFileNames = {{"TrainingOrg_genome.txt.gz", "TrainingOrg_features.txt.gz"}, {"SampleOrg_genome.txt.gz"}};

    public SeqDataParser(String trainingOrgId, String sampleOrgId, String saveDir)
    {
        fetchSeqData(trainingOrgId, sampleOrgId, saveDir, dlFileNames);
        this.trainingSeq = getSeq(dlFileNames[0][0]);
        this.cdsSeq = createCDSSeq(dlFileNames[0][1]);
        this.sampleSeq = getSeq(dlFileNames[1][0]);
        this.totalGenes = totalGenes;
    }

    private void fetchSeqData(String trainingOrgId, String sampleOrgId, String saveDir, String[][] dlFileNames)
    {
        String[] orgs = {trainingOrgId, sampleOrgId};
        String[][] fileNames = {{"_genomic.fna.gz ", "_feature_table.txt.gz"}, {"_genomic.fna.gz"}};

        for (int i=0; i<orgs.length; i++)
        {
            String org = orgs[i];
            String dlLink = "https://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/";
            String orgNumberId = org.substring(4, 13);
            for (int o=0; o<orgNumberId.length()-3+1; o+=3) dlLink += orgNumberId.substring(o, o+3) + "/";
            dlLink += trainingOrgId + "/";

            for (int j=0; j<fileNames[i].length; j++) Downloader.download(saveDir + dlFileNames[i][j], dlLink + fileNames[i][j]);
        }

    }

    private String createCDSSeq(String trainingSeqFeaturesFile)
    {
        String cdsSeq = "";
        int start = 0;
        int end = 0;

        try
        {
            InputStream fileStream = new FileInputStream(trainingSeqFeaturesFile);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
            BufferedReader buffered = new BufferedReader(decoder);
            String l = "";

            while (l != null)
            {
                l = buffered.readLine();
                if (l.isBlank() || l.startsWith("#") || l.startsWith("gene")) continue;
                
                totalGenes++;

                String[] lineContent = l.split("\t");
                start = Integer.parseInt(lineContent[6]);
                end = Integer.parseInt(lineContent[7]);

                for (int i=0; i<start; i++) cdsSeq += "N";
                for (int i=start; i<=end; i++) cdsSeq += "C";
            }

            for (int i=end; i<trainingSeq.length(); i++) cdsSeq += "N";
            fin.close();
        }
        catch (FileNotFoundException ex)
        {
            System.out.println("The file could not be found!");
        }

        return cdsSeq;
    }

    private String getSeq(String seqFile)
    {
        String seqName = "";
        String seq = "";

        try
        {
            InputStream fileStream = new FileInputStream(seqFile);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
            BufferedReader buffered = new BufferedReader(decoder);
            String l = "";

            while (l != null)
            {
                l = buffered.readLine();
                if (l.isBlank()) continue;
                l = l.strip();
                if (l.charAt(0) == '>')
                {
                    seqName = l.substring(1);
                    continue;
                }
                seq += l;
            }

        }
        catch (FileNotFoundException ex)
        {
            System.out.println("The file could not be found!");
        }
        
        return seq;
    }
}
