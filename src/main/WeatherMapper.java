package main;

import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

public class WeatherMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text>{
	public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
        output.collect(new Text(), value);
    }
}

