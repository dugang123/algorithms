package dugang.alg;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class BigSort {

    public static final String TMP_FILE_NAME="tmpSortFile_";

    public static void main(String[] args) throws IOException {
        fileWriter("random",Charset.forName("utf8"),bufferedWriter -> {
            for (int i = 0; i < 1000000; i++) {
                java.util.Random random = new java.util.Random();
                long l = random.nextLong();
                try {
                    bufferedWriter.write(String.valueOf(l));
                    bufferedWriter.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        sort("random","sorted");

    }

    public static void sort(String from,String to) throws IOException {
        sort(from,to,"utf-8",(a,b)->a.compareTo(b),100000);
    }
    
    public static void sort(String from, String to, String encoding, Comparator<String> comparator,int maxlength) throws IOException {
        //clear tmp files
        File[] files = new File(".").listFiles(pathname -> pathname.getName().startsWith(TMP_FILE_NAME));
        Stream.of(files).forEach(file -> file.delete());

        //read big file and split into small files
        fileReader(from,Charset.forName(encoding),bufferedReader -> {
            try {
                String line = null;
                String[] buffer = new String[maxlength];
                int count = 0;
                while ((line = bufferedReader.readLine()) != null) {
                    buffer[count] = line;
                    count++;
                    if (count == maxlength) {
                        sortAndSave(encoding, comparator, buffer, count);
                        count = 0;
                    }
                }
                if (count > 0) {
                    buffer = Arrays.copyOf(buffer,count);
                    sortAndSave(encoding, comparator, buffer, count);
                    count = 0;
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        });

        //recursively mergeSort sort
        while(true) {
            File[] tmpFiles = new File(".").listFiles(pathname -> pathname.getName().startsWith(TMP_FILE_NAME));
            if(tmpFiles.length==0){
                break;
            }
            if (tmpFiles.length == 1) {
                tmpFiles[0].renameTo(new File(to));
                break;
            }

            for (int i = 0; i < tmpFiles.length; i += 2) {
                if (i + 1 < tmpFiles.length) {
                    mergeSort(tmpFiles[i].getAbsolutePath(), tmpFiles[i + 1].getAbsolutePath(), TMP_FILE_NAME + System.currentTimeMillis(), encoding,comparator);
                    tmpFiles[i].delete();
                    tmpFiles[i + 1].delete();
                }
            }
        }
    }

    //mergeSort sort
    private static void mergeSort(String c1, String c2, String merge, String encoding, Comparator<String> comparator) throws IOException {
        fileReader(c1,Charset.forName(encoding),bufferedReader1 -> {
            try {
                fileReader(c2,Charset.forName(encoding),bufferedReader2 -> {
                    try {
                        fileWriter(merge,Charset.forName(encoding),bufferedWriter -> {
                            try {
                                String l1 = bufferedReader1.readLine();
                                String l2 = bufferedReader2.readLine();
                                while(l1!=null || l2!=null){
                                    if(l1!=null && l2!=null){
                                        int compare = comparator.compare(l1, l2);
                                        if(compare>0){
                                            bufferedWriter.write(l2);
                                            bufferedWriter.newLine();
                                            l2 = bufferedReader2.readLine();
                                        }else{
                                            bufferedWriter.write(l1);
                                            bufferedWriter.newLine();
                                            l1 = bufferedReader1.readLine();
                                        }
                                    }else if(l1!=null){
                                        bufferedWriter.write(l1);
                                        bufferedWriter.newLine();
                                        l1 = bufferedReader1.readLine();
                                    }else{
                                        bufferedWriter.write(l2);
                                        bufferedWriter.newLine();
                                        l2 = bufferedReader2.readLine();
                                    }
                                }


                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static void sortAndSave(String encoding, Comparator<String> comparator, String[] list, int count) throws IOException {
        Arrays.sort(list,comparator);
        String tmpfile = TMP_FILE_NAME+System.currentTimeMillis();
        fileWriter(tmpfile,Charset.forName(encoding),bufferedWriter -> {
            try {
                for (int i = 0; i < count; i++) {
                    bufferedWriter.write(list[i]);
                    bufferedWriter.newLine();
                    list[i] = null;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });

    }

    public static void fileWriter(String file, Charset charset, Consumer<BufferedWriter> process) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),charset));
       try {
           process.accept(bw);
       }finally {
           bw.close();
       }
    }
    public static void fileReader(String file,Charset charset,Consumer<BufferedReader> process) throws IOException {
        BufferedReader bw = new BufferedReader(new InputStreamReader(new FileInputStream(file),charset));
        try {
            process.accept(bw);
        }finally {
            bw.close();

        }
    }
}
