package countPerDocument;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
//import java.util.LinkedHashSet;
//import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
//import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
//import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class CountperDocument extends Configured implements Tool {
   
	public static void main(String[] args) throws Exception {
      System.out.println(Arrays.toString(args));
      int res = ToolRunner.run(new Configuration(), new CountperDocument(), args);
      
      System.exit(res);
   }

   @Override
   public int run(String[] args) throws Exception {
      System.out.println(Arrays.toString(args));
      @SuppressWarnings("deprecation")
	Job job = new Job(getConf(), "CountperDocument");
      job.setJarByClass(CountperDocument.class);
      job.setOutputKeyClass(Text.class);
      job.setOutputValueClass(Text.class);
      
      job.setNumReduceTasks(1);
      job.setMapperClass(Map.class);
      job.setReducerClass(Reduce.class);
      
      
      job.setInputFormatClass(TextInputFormat.class);
      job.setOutputFormatClass(TextOutputFormat.class);

      FileInputFormat.addInputPath(job, new Path(args[0]));
      FileOutputFormat.setOutputPath(job, new Path(args[1]));

      job.waitForCompletion(true);
      
      return 0;
   }
   
   public static class Map extends Mapper<LongWritable, Text, Text, Text> {
      private String file_name = new String();
      private Text word = new Text();

      @Override
      public void map(LongWritable key, Text value, Context context)
              throws IOException, InterruptedException {
    	  file_name = ((FileSplit) context.getInputSplit()).getPath().getName();

    	  String line = value.toString();
          line = line.toLowerCase();
          StringTokenizer tokenizer = new StringTokenizer(line, " \"&#()\t\n\r\f,.:;?![]'*-");
          while(tokenizer.hasMoreTokens()) {
        	  String token = tokenizer.nextToken().toLowerCase();
        	  token = token.replaceAll("[^a-z]","");
            word.set(token);
            context.write(new Text(file_name),word);
         }
      }
   }

   public static class Reduce extends Reducer<Text, Text, Text, Text> {
      @Override
      public void reduce(Text key, Iterable<Text> values, Context context)
              throws IOException, InterruptedException {
    	  HashMap<String, Integer> s = new HashMap<String, Integer>();
    	  int sum = 0;
    	 for (Text val : values) {
    		 if(s.get(val.toString())==null){
    			 s.put(val.toString(),1); 
    			 sum++;
    		 }
         }
    	  
         context.write(key, new Text(""+ sum));
      }
   }
}
