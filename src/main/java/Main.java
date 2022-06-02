import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.io.*;
import java.util.Arrays;
import java.util.List;

public class Main extends Application {

    // Инициализация данных
    // Начальные данные
    static int N = 5; // Количество корзин
    static int d = 4; // Количество вытягиваемых шаров за эксперимент
    static int nExp = 10000; // Количество экспериментов
    static int[] boxesAmount = new int[]{250, 240, 240, 200, 300}; // Количество шаров в корзинах
    static int[][] boxes = new int[][]{{92, 103, 46, 9}, {101, 52, 54, 33}, {80, 31, 98, 31},
            {57, 38, 56, 49}, {62, 64, 53, 121}}; // Распределение шаров по корзинам
    // Вспомогательные переменные
    static double[][] ballsRatio = new double[N][d]; // Вероятности вытащить шары для каждой из корзин исходя из знаний о содержимом
    static int[][] ballsAmount = new int[N][d]; // Общий счётчик шаров
    static double[] currentExperiment = new double[]{0, 0, 0, 0}; // Количество вытянутых шаров за эксперимент
    static double[][] primaryProbability = new double[N][N]; // Подсчёт начальных вероятностей гипотез для каждой корзины отдельно
    static Integer[] auxiliaryBoxes = new Integer[N]; // Дополнительные ящики, используемые в расчётах
    // Финальные результаты
    static int[][] hypothesis = new int[N][nExp]; // Гипотезы
    static int[] hypothesisTogether = new int[nExp + 1]; // Гипотезы в совокупности
    static Integer[] mostProbableBoxesSeparately = new Integer[]{0, 0, 0, 0, 0}; // Наиболее вероятное расположение корзин по отдельности
    static int[] mostProbableBoxesTogether = new int[]{0, 0, 0, 0, 0}; // Наиболее вероятно расположение корзин в совокупности
    static Integer[] mostProbableBoxesByFrequency = new Integer[]{0, 0, 0, 0, 0}; // Наиболее вероятное расположение корзин по частоте

    public static void prob() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new
                FileInputStream("input/ball_boxes_arrange.txt")));

        // Инициализация данных и пролистывание ненужных строк в файле
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < d; j++) {
                ballsRatio[i][j] = (double) boxes[i][j] / boxesAmount[i];
                ballsAmount[i][j] = 0;
            }
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                primaryProbability[i][j] = 1.0 / N;
            }
        }
        for (int i = 0; i < 8; i++) {
            reader.readLine();
        }

        // Проведение эксперимента
        for (int i = 0; i < nExp; i++) {
            currentExperiment = new double[]{0, 0, 0, 0};
            String[] experiment = reader.readLine().split("([#]\\s[0-9]+[,]\\s[Bals:]+\\s|[,]\\s)");

            for (String ball : experiment) {
                switch (ball) {
                    case "Red" -> {
                        ballsAmount[i % N][0]++;
                        currentExperiment[0]++;
                    }
                    case "White" -> {
                        ballsAmount[i % N][1]++;
                        currentExperiment[1]++;
                    }
                    case "Black" -> {
                        ballsAmount[i % N][2]++;
                        currentExperiment[2]++;
                    }
                    case "Green" -> {
                        ballsAmount[i % N][3]++;
                        currentExperiment[3]++;
                    }
                }
            }

            for (int j = 0; j < d; j++) {
                double max = 0;
                double x = 0;

                if (currentExperiment[j] != 0) {
                    for (int l = 0; l < currentExperiment[j]; l++) {
                        // Подсчёт условной вероятности события с использованием полученных ранее вероятностей для шаров данного цвета
                        for (int k = 0; k < N; k++) {
                            x += primaryProbability[i % N][k] * ballsRatio[k][j];
                        }
                        // Перерасчёт апостериорных вероятностей
                        for (int k = 0; k < N; k++) {
                            primaryProbability[i % N][k] = primaryProbability[i % N][k] * ballsRatio[k][j] / x;
                            if (primaryProbability[i % N][k] > max) max = primaryProbability[i % N][k];
                        }
                    }
                } else {
                    // Случай, при котором не был вытащен шар данного цвета
                    for (int k = 0; k < N; k++) {
                        x += primaryProbability[i % N][k] * (1.0 - ballsRatio[k][j]);
                    }
                    for (int k = 0; k < N; k++) {
                        primaryProbability[i % N][k] = primaryProbability[i % N][k] * (1.0 - ballsRatio[k][j]) / x;
                        if (primaryProbability[i % N][k] > max) max = primaryProbability[i % N][k];
                    }
                }

                // Поиск наиболее вероятных гипотез о расположении корзин
                for (int k = 0; k < N; k++) {
                    double y = primaryProbability[i % N][k];

                    for (double value : primaryProbability[i % N]) {
                        if (value > 0.9 * y && value < 1.1 * y) {
                            hypothesis[k][i]++;
                        }
                    }
                }
            }

            // Вычисление наиболее вероятного расположения корзин по отдельности
            List<Integer> separate = Arrays.asList(-1, -1, -1, -1, -1);

            for (int j = 0; j < N; j++) {
                double max = 0.0;
                int index = 0;

                for (int k = 0; k < N; k++) {
                    if (primaryProbability[k][j] > max && separate.get(k) == -1) {
                        max = primaryProbability[k][j];
                        index = k;
                    }
                }

                separate.set(index, j);
            }

            mostProbableBoxesSeparately = separate.toArray(new Integer[0]);

            // Вычисление наиболее вероятного расположения корзин в совокупности
            int[] numberOfBox = new int[N];
            double together = 0;
            auxiliaryBoxes = mostProbableBoxesSeparately;

            // Поиск наиболее вероятного расположения корзин среди отобранных гипотез
            for (numberOfBox[0] = auxiliaryBoxes[0] - hypothesis[0][i]; numberOfBox[0] <= auxiliaryBoxes[0] + hypothesis[0][i]; numberOfBox[0]++) {
                for (numberOfBox[1] = auxiliaryBoxes[1] - hypothesis[1][i]; numberOfBox[1] <= auxiliaryBoxes[1] + hypothesis[1][i]; numberOfBox[1]++) {
                    for (numberOfBox[2] = auxiliaryBoxes[2] - hypothesis[2][i]; numberOfBox[2] <= auxiliaryBoxes[2] + hypothesis[2][i]; numberOfBox[2]++) {
                        for (numberOfBox[3] = auxiliaryBoxes[3] - hypothesis[3][i]; numberOfBox[3] <= auxiliaryBoxes[3] + hypothesis[3][i]; numberOfBox[3]++) {
                            for (numberOfBox[4] = auxiliaryBoxes[4] - hypothesis[4][i]; numberOfBox[4] <= auxiliaryBoxes[4] + hypothesis[4][i]; numberOfBox[4]++) {
                                if (numberOfBox[0] != numberOfBox[1] && numberOfBox[0] != numberOfBox[2] && numberOfBox[0] != numberOfBox[3] && numberOfBox[0] != numberOfBox[4]
                                        && numberOfBox[1] != numberOfBox[2] && numberOfBox[1] != numberOfBox[3] && numberOfBox[1] != numberOfBox[4]
                                        && numberOfBox[2] != numberOfBox[3] && numberOfBox[2] != numberOfBox[4]
                                        && numberOfBox[3] != numberOfBox[4]
                                        && numberOfBox[0] >= 0 && numberOfBox[1] >= 0 && numberOfBox[2] >= 0 && numberOfBox[3] >= 0 && numberOfBox[4] >= 0
                                        && numberOfBox[0] < 5 && numberOfBox[1] < 5 && numberOfBox[2] < 5 && numberOfBox[3] < 5 && numberOfBox[4] < 5) {
                                    double max = 0.0;
                                    double value = 0.0;
                                    double factorialDivision = 1.0;
                                    int ball = 0;

                                    hypothesisTogether[i]++;

                                    for (int j = 0; j < N; j++) {
                                        if (primaryProbability[j][numberOfBox[j]] > max) {
                                            max = primaryProbability[j][numberOfBox[j]];
                                        }
                                    }

                                    for (int j = 0; j < N; j++) {
                                        for (int k = 0; k < numberOfBox[j]; k++) {
                                            ball++;
                                            factorialDivision *= (double) ball / (k + 1);
                                            value += Math.exp(primaryProbability[j][numberOfBox[j]] - max);
                                        }
                                    }

                                    value = max + Math.log(value);
                                    double togetherVariant = factorialDivision * value;

                                    if (togetherVariant > together) {
                                        together = togetherVariant;
                                        mostProbableBoxesTogether = numberOfBox.clone();
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        // Вычисление наиболее вероятного расположения корзин по частоте
        List<Integer> order = Arrays.asList(-1, -1, -1, -1, -1);

        for (int i = 0; i < N; i++) {
            int[] current = new int[]{0, 0, 0, 0, 0, 0};
            int sum = 0;

            for (int j = 0; j < d; j++) {
                sum += ballsAmount[i][j];
            }

            for (int j = 0; j < d; j++) {
                double ratio = (double) ballsAmount[i][j] / sum;
                int mostProbableBox = 0;
                double difference = 1;

                for (int k = 0; k < N; k++) {
                    if (Math.abs(ballsRatio[k][j] - ratio) < difference) {
                        mostProbableBox = k;
                        difference = Math.abs(ballsRatio[k][j] - ratio);
                    }
                }

                current[mostProbableBox] += 1;
            }

            int max = -1;
            int box = -1;
            for (int j = 0; j < N; j++) {
                if (current[j] > max && !order.contains(j)) {
                    max = current[j];
                    box = j;
                }
            }

            order.set(i, box);
        }

        mostProbableBoxesByFrequency = order.toArray(new Integer[0]);
        for (int i = 0; i < N; i++) {
            double sum = 0;

            for (int j = 0; j < N; j++) {
                sum += primaryProbability[i][j];
            }
            for (int j = 0; j < N; j++) {
                primaryProbability[i][j] = primaryProbability[i][j] / sum;
            }
        }

        System.out.println("Результаты при nExp = " + nExp + ":");
        System.out.println("\n\t1) Наиболее вероятное разложение корзин по частоте вытаскивания:");
        System.out.println("\t   Box 1 = " + mostProbableBoxesByFrequency[0] + "\t" + "Box 2 = " + mostProbableBoxesByFrequency[1] +
                "\t" + "Box 3 = " + mostProbableBoxesByFrequency[2] +  "\t" + "Box 4 = " + mostProbableBoxesByFrequency[3] +
                "\t" + "Box 5 = " + mostProbableBoxesByFrequency[4]);
        System.out.println("\n\t2) Наиболее вероятное разложение корзин в совокупности:");
        System.out.println("\t   Box 1 = " + mostProbableBoxesTogether[0] + "\t" + "Box 2 = " + mostProbableBoxesTogether[1] +
                "\t" + "Box 3 = " + mostProbableBoxesTogether[2] + "\t" + "Box 4 = " + mostProbableBoxesTogether[3] +
                "\t" + "Box 5 = " +  mostProbableBoxesTogether[4]);
        System.out.println("\n\t3) Наиболее вероятное разложение корзин по отдельности:");
        System.out.println("\t   Box 1 = " + mostProbableBoxesSeparately[0] + "\t" + "Box 2 = " + mostProbableBoxesSeparately[1] +
                "\t" + "Box 3 = " + mostProbableBoxesSeparately[2] + "\t" + "Box 4 = " + mostProbableBoxesSeparately[3] +
                "\t" + "Box 5 = " + mostProbableBoxesSeparately[4]);
    }

    public LineChart<Number, Number> probabilityLineChart() {
        XYChart.Series first = new XYChart.Series();
        XYChart.Series second = new XYChart.Series();
        XYChart.Series third = new XYChart.Series();
        XYChart.Series fourth = new XYChart.Series();
        XYChart.Series fifth = new XYChart.Series();
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

        lineChart.setTranslateX(0);
        lineChart.setTranslateY(0);
        xAxis.setLabel("Box");
        yAxis.setLabel("Probability");
        lineChart.setTitle("Probability");
        first.setName("First box");
        second.setName("Second box");
        third.setName("Third box");
        fourth.setName("Fourth box");
        fifth.setName("Fifth box");

        for (int i = 0; i < N; i++) {
            first.getData().add(new XYChart.Data(i, primaryProbability[i][0]));
            second.getData().add(new XYChart.Data(i, primaryProbability[i][1]));
            third.getData().add(new XYChart.Data(i, primaryProbability[i][2]));
            fourth.getData().add(new XYChart.Data(i, primaryProbability[i][3]));
            fifth.getData().add(new XYChart.Data(i, primaryProbability[i][4]));
        }

        lineChart.getData().addAll(first, second, third, fourth, fifth);

        return lineChart;
    }

    public LineChart<Number, Number> frequencyLineChart() {
        XYChart.Series first = new XYChart.Series();
        XYChart.Series second = new XYChart.Series();
        XYChart.Series third = new XYChart.Series();
        XYChart.Series fourth = new XYChart.Series();
        XYChart.Series fifth = new XYChart.Series();
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

        lineChart.setTranslateX(0);
        lineChart.setTranslateY(0);
        xAxis.setLabel("Box");
        yAxis.setLabel("Number of boxes");
        lineChart.setTitle("Frequency");
        first.setName("First box");
        second.setName("Second box");
        third.setName("Third box");
        fourth.setName("Fourth box");
        fifth.setName("Fifth box");

        int sum = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < d; j++) {
                sum += ballsAmount[i][j];
            }
        }

        first.getData().add(new XYChart.Data(1, mostProbableBoxesByFrequency[0]));
        second.getData().add(new XYChart.Data(2, mostProbableBoxesByFrequency[1]));
        third.getData().add(new XYChart.Data(3, mostProbableBoxesByFrequency[2]));
        fourth.getData().add(new XYChart.Data(4, mostProbableBoxesByFrequency[3]));
        fifth.getData().add(new XYChart.Data(5, mostProbableBoxesByFrequency[4]));

        lineChart.getData().addAll(first, second, third, fourth, fifth);

        return lineChart;
    }

    public LineChart<Number, Number> togetherLineChart() {
        XYChart.Series first = new XYChart.Series();
        XYChart.Series second = new XYChart.Series();
        XYChart.Series third = new XYChart.Series();
        XYChart.Series fourth = new XYChart.Series();
        XYChart.Series fifth = new XYChart.Series();
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

        lineChart.setTranslateX(0);
        lineChart.setTranslateY(0);
        xAxis.setLabel("Box");
        yAxis.setLabel("Number of boxes");
        lineChart.setTitle("Number of boxes (together)");
        first.setName("First box");
        second.setName("Second box");
        third.setName("Third box");
        fourth.setName("Fourth box");
        fifth.setName("Fifth box");

        first.getData().add(new XYChart.Data(1, mostProbableBoxesTogether[0]));
        second.getData().add(new XYChart.Data(2, mostProbableBoxesTogether[1]));
        third.getData().add(new XYChart.Data(3, mostProbableBoxesTogether[2]));
        fourth.getData().add(new XYChart.Data(4, mostProbableBoxesTogether[3]));
        fifth.getData().add(new XYChart.Data(5, mostProbableBoxesTogether[4]));

        lineChart.getData().addAll(first, second, third, fourth, fifth);

        return lineChart;
    }

    public LineChart<Number, Number> separatelyLineChart() {
        XYChart.Series first = new XYChart.Series();
        XYChart.Series second = new XYChart.Series();
        XYChart.Series third = new XYChart.Series();
        XYChart.Series fourth = new XYChart.Series();
        XYChart.Series fifth = new XYChart.Series();
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

        lineChart.setTranslateX(0);
        lineChart.setTranslateY(0);
        xAxis.setLabel("Box");
        yAxis.setLabel("Number of boxes");
        lineChart.setTitle("Number of boxes (separately)");
        first.setName("First box");
        second.setName("Second box");
        third.setName("Third box");
        fourth.setName("Fourth box");
        fifth.setName("Fifth box");

        first.getData().add(new XYChart.Data(1, mostProbableBoxesSeparately[0]));
        second.getData().add(new XYChart.Data(2, mostProbableBoxesSeparately[1]));
        third.getData().add(new XYChart.Data(3, mostProbableBoxesSeparately[2]));
        fourth.getData().add(new XYChart.Data(4, mostProbableBoxesSeparately[3]));
        fifth.getData().add(new XYChart.Data(5, mostProbableBoxesSeparately[4]));

        lineChart.getData().addAll(first, second, third, fourth, fifth);


        return lineChart;
    }

    public LineChart<Number, Number> hypothesisOfBalls() {
        XYChart.Series hypothesisLine = new XYChart.Series();

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTranslateX(0);
        lineChart.setTranslateY(0);

        xAxis.setLabel("nExp");
        yAxis.setLabel("Hypothesis");
        lineChart.setTitle("Hypothesis - fifth box");
        hypothesisLine.setName("Fifth box");

        for (int i = 0; i < nExp; i++) {
            hypothesisLine.getData().add(new XYChart.Data(i, hypothesis[4][i]));
        }

        lineChart.getData().addAll(hypothesisLine);

        return lineChart;
    }

    public LineChart<Number, Number> hypothesisOfBallsTogether() {
        XYChart.Series hypothesisLine = new XYChart.Series();

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTranslateX(0);
        lineChart.setTranslateY(0);

        xAxis.setLabel("nExp");
        yAxis.setLabel("Hypothesis");
        lineChart.setTitle("Hypothesis (together)");
        hypothesisLine.setName("hypothesis");

        for (int i = 0; i < nExp; i++) {
            hypothesisLine.getData().add(new XYChart.Data(i, hypothesisTogether[i]));
        }

        lineChart.getData().addAll(hypothesisLine);

        return lineChart;
    }

    public static void main(String[] args) throws IOException {
        prob();
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Расчётное задание №1");
        stage.setHeight(850);
        stage.setWidth(1300);

        GridPane gridPane = new GridPane();
        gridPane.add(probabilityLineChart(), 0, 0);
        gridPane.add(frequencyLineChart(), 0, 1);
        gridPane.add(togetherLineChart(), 1, 0);
        gridPane.add(separatelyLineChart(), 1, 1);

        Scene scene = new Scene(hypothesisOfBalls());
        stage.setScene(scene);

        stage.show();
    }
}