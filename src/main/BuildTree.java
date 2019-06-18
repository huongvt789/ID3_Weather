package main;

import java.util.*;
import java.util.Map.Entry;

public class BuildTree {
    public static int feat_count = 0;
    public static LinkedHashMap<String, String> p = new LinkedHashMap<>();
    public static HashMap<String, Integer> nodes = new HashMap<>();
    //Tính độ lợi thông tin.
    public static HashMap<Integer, Double> gain = new HashMap<>();
    // Đếm số lượng thuộc tính phân lớp tương ứng với thuộc tính child_node đó.
    public static HashMap<String, Double> feature_count = new HashMap<>();
    //Dếm số lượng phân lớp phân lớp tương ứng của với thuộc tính root node.
    public static HashMap<String, Double> outcome_count = new HashMap<>();
    public static HashMap<Integer, String> intermediate = new HashMap<>();
    public static ArrayList<String> tree = new ArrayList<>();

    /*
     * Hàm kiểu mảng để buld dữ liệu.
     */
    public static ArrayList<String> build(LinkedHashMap<String, String> g, ArrayList<String> data, int size) {
        String root_node = _choseNode(g, data, size);
        
        // lấy các thuộc tính của đặc trưng được chọn
        ArrayList<String> features = new ArrayList<>();
        for (Entry<String, Double> e : feature_count.entrySet()) {
            String key = e.getKey().split("\\,")[0];
            if (key.equals(root_node)) {
                String feature_name = e.getKey().split("\\,")[1];
                if (!features.contains(feature_name)) {
                    features.add(feature_name);
                }
            }
        }
        
        /*
         * Xác định nút lá cho nút gốc.
         */
        features.forEach(feature -> {
            ArrayList<String> child_node_data = new ArrayList<>();
            data.forEach(row -> {
                if (feature.equals(row.split("\\,")[Integer.parseInt(root_node)])) {
                    child_node_data.add(row);
                }
            });
            _clearData();
            String child_node = _choseNode(g, child_node_data, child_node_data.size());
            
            LinkedHashMap<String, Double> child_node_feature = new LinkedHashMap<>();
            /*
             * Xác định nút lá (phân lớp cho thuộc tính) tạo nên cây quyết định.
             */
            child_node_data.forEach(row -> {
                String[] row_data = row.split("\\,");
                String key = row_data[Integer.parseInt(child_node)] + "," + row_data[row_data.length - 1];
                if (child_node_feature.containsKey(key)) {
                    child_node_feature.put(key, child_node_feature.get(key) + 1);
                } else {
                    child_node_feature.put(key, (double) 1);
                }
            });
            
            LinkedHashMap<String, String> handle_child_node_feature = _handleChildNodeFeature(child_node_feature);
            handle_child_node_feature.entrySet().forEach(entry->{
                String[] leaf_node = entry.getValue().split("\\,");
            
                tree.add(root_node + "," + feature + "," + child_node + "," + leaf_node[0] + "=>" + leaf_node[1]);
            });
        });
        return tree;
    }
    /*
     * Xóa các biến toàn cục để tránh xảy ra sự trùng lặp dữ liệu.
     * Khi các biến được chuyển tới quá trình gọi đệ quy => Tránh đệ quy vô hạn.
     */
    private static void _clearData() {
        gain.clear();
        intermediate.clear();
        feature_count.clear();
        outcome_count.clear();
    }
    
    /*
     * Hàm xử lý chọn nút cho cây.
     * Tính entroy và gian.
     */
    private static String _choseNode(LinkedHashMap<String, String> g, ArrayList<String> data, int size) {
        if (p.size() == 0)
            p.putAll(g);
        
        if (feat_count == 0)
            feat_count = data.get(0).split("\\,").length - 1;

        // Xử lý dữ liệu đầu vào
        // Với q là số thứ tự của thuộc tính 0: outlook, 1: temperature, 2: humidity, 3: wind
        // Phân tách từng dòng của dữ liệu đầu vào bằng dấu ","
        for (int i = 0; i < data.size(); i++) {
            String[] input = data.get(i).split("\\,");

            // input.length - 1: số thự tự của thuộc tính cuối cùng của dữ liệu đầu vào
            // trường hợp này là thuộc tính play
            for (int q = 0; q < input.length; q++) {
                if (q == input.length - 1) {

                    // Đếm tất cả số lượng yes và no của dữ liệu đầu vào.
                    if (outcome_count.containsKey(input[q]))
                        outcome_count.put(input[q], outcome_count.get(input[q]) + 1);
                    else
                        outcome_count.put(input[q], (double) 1);
                } else {

                    // Đếm số lượng yes và no của từng đặc trưng với key là [số thứ tự của thuộc tính, tên đặc trưng của thuộc tính, yes hoặc no]
                    // ví dụ: 0, sunny, no 3.0
                    // là outlook, sunny, no: số lượng no là 3
                    // ví dụ: 0, sunny, yes 2.0
                    // là outlook, sunny, no: số lượng yes là 2
                    if (feature_count.containsKey(q + "," + input[q] + "," + input[input.length - 1]))
                        feature_count.put(q + "," + input[q] + "," + input[input.length - 1], feature_count.get(q + "," + input[q] + "," + input[input.length - 1]) + 1);
                    else
                        feature_count.put(q + "," + input[q] + "," + input[input.length - 1], (double) 1);
                }
            }
        }

        // Tạo dữ liệu trung gian
        for (Entry<String, Double> e : feature_count.entrySet()) {
            // key là giá trị của thuộc tính
            // 0: outlook, 1: temperature, 2: humidity, 3: wind
            String[] key = e.getKey().split("\\,");
            
            // giá trị của biến trung gian là tổng số lượng của từng đặc trưng của thuộc tính
            // ví dụ: 0 sunny:5.0
            // là outlook, sunny số lượng outlook sunny là 5
            if (intermediate.containsKey(Integer.parseInt(key[0])))
                intermediate.put(Integer.parseInt(key[0]), intermediate.get(Integer.parseInt(key[0])) + "," + key[1] + ":" + (e.getValue()));
            else
                intermediate.put(Integer.parseInt(key[0]), key[1] + ":" + e.getValue());
        }

        // Tính H(S)
        double entropy = 0.0;
        for (Entry<String, Double> e : outcome_count.entrySet()) {
            double p = ((e.getValue() / size));
            entropy += -(p * (Math.log(p) / Math.log(2)));
        }

        // khởi tạo gain cho từng thuộc tính
        for (int i = 0; i < data.get(0).split("\\,").length - 1; i++) {
            gain.put(i, entropy);
        }

        // Tính entropy của từng đặc trưng trong mỗi thuộc tính
        // chạy vòng lặp của từng đặc trưng
        for (Entry<Integer, String> e : intermediate.entrySet()) {
            if (gain.containsKey(e.getKey())) {

                double info_gain_except_the_entropy = 0.0;
                String[] counts = e.getValue().split("\\,");

                // feat dùng để lưu số lượng yes no của thuộc tính của đặc trưng
                // ví dụ: sunny 3.0, 2.0: số lượng no là 3, số lượng yes là 2
                HashMap<String, String> feat = new HashMap<String, String>();
                for (int j = 0; j < counts.length; j++) {
                    if (feat.containsKey(counts[j].split("\\:")[0]))
                        feat.put(counts[j].split("\\:")[0], feat.get(counts[j].split("\\:")[0]) + "," + counts[j].split("\\:")[1]);
                    else
                        feat.put(counts[j].split("\\:")[0], counts[j].split("\\:")[1]);
                }

                for (Entry<String, String> r : feat.entrySet()) {
                    String[] c = r.getValue().split("\\,");
                    int num = 0;
                    for (int i = 0; i < c.length; i++) {
                        num += Double.parseDouble(c[i]);
                    }

                    // tính entropy của từng thuộc tính
                    double ent = 0.0;
                    for (int i = 0; i < c.length; i++) {
                        double p = Double.parseDouble(c[i]) / (double) num;
                        ent += -(p * (Math.log(p) / Math.log(2)));
                    }

                    // tính info của đặc trưng
                    double cs = num / (double) size;
                    info_gain_except_the_entropy += ent * cs;
                }

                // tính gain của đặc trưng
                gain.put(e.getKey(), gain.get(e.getKey()) - info_gain_except_the_entropy);
            }
        }

        // Tìm nút gốc
        return _getMaxGain(gain)[0];
    }

    /*
     * Tính max gain đúng như tên của hàm.
     */
    private static String[] _getMaxGain(HashMap<Integer, Double> gain) {
        int maxKey = -1;
        Double maxValue = Double.NEGATIVE_INFINITY;
        for (Entry<Integer, Double> e : gain.entrySet()) {
            if (e.getValue() > maxValue && !nodes.containsKey(String.valueOf(e.getKey()))) {
                maxKey = e.getKey();
                maxValue = e.getValue();
            }
        }
        String[] s = new String[2];
        s[0] = String.valueOf(maxKey);
        s[1] = String.valueOf(maxValue);
        return s;
    }
    
    /*
     * Xác định nút lá phân lớp.
     */
    private static LinkedHashMap<String, String> _handleChildNodeFeature(LinkedHashMap<String, Double> child_node_feature) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        child_node_feature.entrySet().forEach(entry -> {
            String[] feature = entry.getKey().split("\\,");
            double value = entry.getValue();
            if (result.containsKey(feature[0])) {
                double max_value = Double.parseDouble(result.get(feature).split("\\,")[2]);
                if (value > max_value) {
                    result.put(feature[0], entry.getKey() + "," + entry.getValue());
                }
            } else {
                result.put(feature[0], entry.getKey() + "," + entry.getValue());
            }
        });
        return result;
    }
}