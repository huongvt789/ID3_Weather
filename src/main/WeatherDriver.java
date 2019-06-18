package main;

import java.sql.Timestamp;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;

public class WeatherDriver {

	private static String inputFile = "input/demoID3.csv";
	private static String outputFile = "ouput/output" + new Timestamp(System.currentTimeMillis());

	public static void main(String[] args) {
		JobClient my_client = new JobClient();
		// Create a configuration object for the job
		JobConf job_conf = new JobConf(WeatherDriver.class);

		// Set a name of the Job
		job_conf.setJobName(" Project ");

		// Specify data type of output key and value
		job_conf.setOutputKeyClass(Text.class);
		job_conf.setOutputValueClass(Text.class);

		// Specify names of Mapper and Reducer Class
		job_conf.setMapperClass(WeatherMapper.class);
		job_conf.setReducerClass(WeatherReducer.class);

		// Specify formats of the data type of Input and output
		job_conf.setInputFormat(TextInputFormat.class);
		job_conf.setOutputFormat(TextOutputFormat.class);

		// Set input and output directories using command line arguments, 
		
		FileInputFormat.setInputPaths(job_conf, new Path(inputFile));
		FileOutputFormat.setOutputPath(job_conf, new Path(outputFile));
		
		my_client.setConf(job_conf);
		try {
			// Run the job 
			JobClient.runJob(job_conf);
	    	System.out.println("Decission Tree complete!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}