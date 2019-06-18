package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class WeatherReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {


    private static int count = 0;
    private static ArrayList<String> input = new ArrayList<>();
    private static OutputCollector<Text, Text> collector = null;

    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
        while (values.hasNext()) {
            input.add(values.next().toString());
            count++;
        }
        collector = output;
    }

    //Chạy toàn bộ dữ lấy tổng bản ghi. Sau đó gọi close để xử lý.
    @Override
    public void close() throws IOException {
        LinkedHashMap<String, String> g = new LinkedHashMap<>();
        ArrayList<String> tree = BuildTree.build(g, input, count);
        tree.forEach(node -> {
            try {
                collector.collect(null, new Text(node));
            } catch (IOException exception) {
                System.out.println(exception);
            }
        });
    }
}
