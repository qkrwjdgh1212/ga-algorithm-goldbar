
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class GoldbarQ {

    public static void main(String[] args) {

        GAController gc = new GAController();
        gc.test();
        while (gc.flag) {
            gc.t += 1;
            gc.select();
            gc.alter();
            gc.test();
        }
        System.out.println("best sum : " + gc.best_chr.sum +
                " best value : " + gc.best_chr.val);

    }

    static class GAController {
        final float select_ratio = 0.5f;
        final int population_size = 12;
        Chromosome[] population;
        final float mutation_ratio = 0.05f;
        final int max_w = 10;
        int t = 0;
        boolean flag = true;
        Gold[] golds;
        ArrayList<Chromosome> tmp_list = new ArrayList<Chromosome>();
        ArrayList<Chromosome> deletion_list = new ArrayList<Chromosome>();
        int best_unchange_cnt = 0;
        Chromosome best_chr;
        final int MAX_UNCHANGED = 40;

        public GAController() {
            makeGold();
            makeChromosomes();
        }

        public void makeChromosomes() {
            population = new Chromosome[population_size];
            int cnt = 0;
            while (cnt < population_size) {
                Chromosome c = new Chromosome();
                int[] code = new int[golds.length];
                for (int i = 0; i < golds.length; i++) {
                    double rand_v = Math.random();
                    if (rand_v > 0.5) {
                        code[i] = 1;
                        c.sum += golds[i].w;
                        c.val += golds[i].v;
                    } else {
                        code[i] = 0;
                    }
                }
                if (c.sum <= 10) {
                    c.code = code;
                    population[cnt++] = c;
                }
            }
        }

        // fitness
        public void test() {
            // val 기준으로 정렬
            Arrays.sort(population);
            // best chromosome
            if (best_chr == null) {
                best_chr = population[0];
            } else {
                if (best_chr.val < population[0].val) {
                    best_chr = population[0].clone();
                    best_unchange_cnt = 0;
                } else {
                    best_unchange_cnt++;
                }
            }
            // best chromosome 이 MAX_UNCHANGED 세대 이상 변화가
            // 없다면 알고리즘을 끝내라.
            if (best_unchange_cnt > MAX_UNCHANGED) {
                flag = false;
            }
        }

        public void alter() {
            crossover();
            double rand_v = Math.random();
            if (rand_v < mutation_ratio) {
                mutation();
            }
        }

        private void crossover() {
            boolean[] visited = new boolean[(int) (population_size * select_ratio)];
            combination(population, visited, 0,
                    (int) (population_size * select_ratio), 2);
            Collections.sort(tmp_list);
            int idx = 0;
            int d_idx = 0;
            for (int i = (int) (population_size * select_ratio); i < population_size; i++) {
                if (idx < tmp_list.size()) {
                    population[i] = tmp_list.get(idx++);
                } else {
                    population[i] = deletion_list.get(d_idx++);
                }
            }
            tmp_list.clear();

        }

        private void combination(Chromosome[] arr, boolean[] visited, int start, int n, int r) {
            if (r == 0) {
                // 교배를 할 두 크로모좀
                Chromosome[] selected_chr = new Chromosome[2];
                int cnt = 0;
                for (int i = 0; i < n; i++) {
                    if (visited[i] == true)
                        selected_chr[cnt++] = arr[i].clone();
                }
                // partially matched crossover
                // 앞에서부터 2개 crossover
                for (int i = 0; i < golds.length; i++) {
                    swap(selected_chr[0], selected_chr[1], i);
                    if (checkSum(selected_chr[0]) && checkSum(selected_chr[1])) {

                        cnt--;
                    } else {
                        swap(selected_chr[0], selected_chr[1],
                                i);
                    }
                    if (cnt == 0)
                        break;

                }
                if (cnt <= 1) {
                    tmp_list.add(selected_chr[0]);
                    tmp_list.add(selected_chr[1]);
                }
                return;

            }
            for (int i = start; i < n; i++) {
                visited[i] = true;
                combination(arr, visited, i + 1, n, r - 1);
                visited[i] = false;
            }
        }

        private void mutation() {
            int mutation = (int) ((Math.random() *
                    population_size));
            int idx = (int) (Math.random() * golds.length);
            population[mutation].code[idx] = (population[mutation].code[idx] == 1) ? 0 : 1;
        }

        private void swap(Chromosome ch1, Chromosome ch2,
                int i) {
            int tmp = ch1.code[i];
            ch1.code[i] = ch2.code[i];
            ch2.code[i] = tmp;
            ch1.setSumVal();
            ch2.setSumVal();
        }

        private boolean checkSum(Chromosome ch) {
            if (ch.sum > max_w)
                return false;
            return true;
        }

        public void select() {
            // 상위 select_ratio 만큼만 선택
            for (int i = (int) (population_size * select_ratio); i < population_size; i++) {
                deletion_list.add(population[i].clone());
                population[i] = null;
            }
        }

        class Gold {
            int w;
            int v;

            public Gold(int w, int v) {
                this.w = w;
                this.v = v;
            }
        }

        class Chromosome implements Comparable<Chromosome>, Cloneable {
            int sum = 0;
            int val = 0;
            int[] code;

            @Override
            public int compareTo(Chromosome o) {
                return o.val - this.val;
            }

            public void setSumVal() {
                this.sum = 0;
                this.val = 0;
                for (int i = 0; i < code.length; i++) {
                    if (code[i] == 1) {
                        this.sum += golds[i].w;
                        this.val += golds[i].v;
                    }
                }
            }

            // 깊은 복사를 위한 메소드
            public Chromosome clone() {
                Chromosome ch = null;
                try {
                    ch = (Chromosome) super.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                return ch;
            }

        }

        public void makeGold() {
            this.golds = new Gold[6];
            Gold gold1 = new Gold(4, 6);
            Gold gold2 = new Gold(4, 7);
            Gold gold3 = new Gold(2, 4);
            Gold gold4 = new Gold(1, 3);
            Gold gold5 = new Gold(6, 9);
            Gold gold6 = new Gold(3, 5);
            golds[0] = gold1;
            golds[1] = gold2;
            golds[2] = gold3;
            golds[3] = gold4;
            golds[4] = gold5;
            golds[5] = gold6;
        }
    }

}