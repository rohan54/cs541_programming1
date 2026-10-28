package org.example;

import java.util.*;

public class Main {

    public enum Heuristic{
        MANHATTAN,
        NUMBER_OF_MISMATCH,
        SLD
    }

    public enum Algorithm{
        BEST_SEARCH_FIRST,
        A_STAR
    }

    public static boolean isReachable(int[][] a, int[][] b){
        int inversionA = checkInversions(a);
        int inversionB = checkInversions(b);
        //same parity
//        System.out.println("inversionA:"+inversionA+" inversionB:"+inversionB);
        return (inversionA%2==0) == (inversionB%2==0);
    }

    private static int checkInversions(int[][] a) {
        List<Integer> list = new ArrayList<>();
        int inversion = 0;
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                int current = a[i][j];
                if(current!=0) {
                    for (Integer seen : list) {
                        if (current < seen)
                            inversion++;
                    }
                    list.add(current);
                }
            }
        }
        return inversion;
    }

    private static double heuristicSLD(int[][] input, int[][] target){
        if(input.length!=target.length || input[0].length!=target[0].length){
            throw new RuntimeException("input target mismatch");
        }
        Map<Integer,int[]> positions = new HashMap<>();
        for (int i = 0; i < target.length; i++) {
            for (int j = 0; j < target[0].length; j++) {
                positions.put(target[i][j], new int[] {i,j});
            }
        }
        int result = 0;
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                int val = input[i][j];
                int[] xy = positions.get(val);
                result += Math.sqrt(Math.pow(xy[0]-i,2)+Math.pow(xy[1]-j,2));
            }
        }
        return result;
    }

    private static int heuristicNumberOfMismatch(int[][] input, int[][] target){
        if(input.length!=target.length || input[0].length!=target[0].length){
            throw new RuntimeException("input target mismatch");
        }
        int result = 0;
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                if(input[i][j]!=0 && input[i][j]!=target[i][j]){
                    result++;
                }
            }
        }
        return result;
    }

    private static int heuristicManhattanDistance(int[][] input, int[][] target){
        if(input.length!=target.length || input[0].length!=target[0].length){
            throw new RuntimeException("input target mismatch");
        }
        Map<Integer,int[]> positions = new HashMap<>();
        for (int i = 0; i < target.length; i++) {
            for (int j = 0; j < target[0].length; j++) {
                positions.put(target[i][j], new int[] {i,j});
            }
        }
        int result = 0;
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                int val = input[i][j];
                int[] xy = positions.get(val);
                result += Math.abs(xy[0]-i)+ Math.abs(xy[1]-j);
            }
        }
        return result;
    }

    public static int[][] decodeArray(String s, int n){
//        System.out.println("Converting string " + s + "to array");
        int i =0; int j =0;
        int[][] result = new int[n][n];
        for (int k = 0; k < s.length(); k++) {
            char c = s.charAt(k);
            i = k/n;
            j = k%n;
            if(c=='b'){
                result[i][j] = 0;
            }else{
                result[i][j] = c-'0';
            }
        }
        return result;
    }

    public static String encodeArray(int[][] input, int n){
//        System.out.println("Encoding array to string");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                int val = input[i][j];
                if(val==0)
                    sb.append("b");
                else
                    sb.append(val);
            }
        }
        return sb.toString();
    }

    public static String fixedInputs(int i){
        return new String[]{"b52183476", "41376852b", "b23156784", "3461b5782", "5134b2786"}[i];
    }

    public static String shuffleString(String s){
        List<Character> characters = new ArrayList<>();
        for(char c:s.toCharArray()){
            characters.add(c);
        }
        Collections.shuffle(characters);
        StringBuilder sb = new StringBuilder();
        for(char c: characters){
            sb.append(c);
        }
        return sb.toString();
    }

    public static List<String> nextStates(String s, int n){
        List<String> nextStates = new ArrayList<>();
        int[][] matrix = decodeArray(s, n);
        int b = s.indexOf('b');
        int bi = b/n;
        int bj = b%n;
        if(matrix[bi][bj]!=0)
            throw new RuntimeException("invalid state for location of b/0");
        List<int[]> moves = Arrays.asList(new int[]{0,1},new int[]{1,0},new int[]{0,-1},new int[]{-1,0});
        for(int[] move: moves){
            int i = move[0]+bi;
            int j = move[1]+bj;
            if(i>=0 && i<n && j>=0 && j<n){
                matrix[bi][bj] = matrix[i][j];
                matrix[i][j] = 0;
                nextStates.add(encodeArray(matrix,n));
                matrix[i][j] = matrix[bi][bj];
                matrix[bi][bj] = 0;
            }
        }
        return nextStates;
    }

    private static List<String> reconstructPath(
            String goal, Map<String, String> parent) {

        List<String> path = new ArrayList<>();

        for (String state = goal; state != null; state = parent.get(state)) {
            path.add(state);
        }

        Collections.reverse(path);
        return path;
    }

    public record State(String s, int distanceToReach){}

    public static List<String> findSolution(String initial,
                                            String goal,
                                            int n,
                                            Heuristic heuristic,
                                            Algorithm algo){
        if(!isReachable(decodeArray(initial,n),decodeArray(goal,n))){
            return Collections.emptyList();
        }
        Map<String, String> parent = new HashMap<>();
        Map<String,State> exploredState = new HashMap<>();
        parent.put(initial, null);
        Comparator<String> stringComparator = switch (heuristic) {
            case MANHATTAN ->
                    Comparator.comparingInt(a -> heuristicManhattanDistance(decodeArray(a, n), decodeArray(goal, n)));
            case NUMBER_OF_MISMATCH ->
                    Comparator.comparingInt(a -> heuristicNumberOfMismatch(decodeArray(a, n), decodeArray(goal, n)));
            case SLD -> Comparator.comparingDouble(a -> heuristicSLD(decodeArray(a, n), decodeArray(goal, n)));
            default -> throw new RuntimeException("Heuristic not known");
        };
        Comparator<String> stringComparatorForA = switch (heuristic) {
            case MANHATTAN ->
                    Comparator.comparingInt(a -> heuristicManhattanDistance(decodeArray(a, n), decodeArray(goal, n))+exploredState.get(a).distanceToReach);
            case NUMBER_OF_MISMATCH ->
                    Comparator.comparingInt(a -> heuristicNumberOfMismatch(decodeArray(a, n), decodeArray(goal, n))+exploredState.get(a).distanceToReach);
            case SLD -> Comparator.comparingDouble(a -> heuristicSLD(decodeArray(a, n), decodeArray(goal, n))+exploredState.get(a).distanceToReach);
            default -> throw new RuntimeException("Heuristic not known");
        };
        PriorityQueue<String> pq;
        if(algo.equals(Algorithm.BEST_SEARCH_FIRST)) {
            pq = new PriorityQueue<>(stringComparator);
        } else {
            pq = new PriorityQueue<>(stringComparatorForA);
        }
        exploredState.put(initial,new State(initial, 0));
        pq.add(initial);
        int iterationCapped = 200_000;
        List<String> trace = new ArrayList<>();
        while(!pq.isEmpty() && iterationCapped>0){
            String poll = pq.poll();
            int distanceToReach = poll.equals(initial) ? 0 : exploredState.get(parent.get(poll)).distanceToReach + 1;
            State state = new State(poll, distanceToReach);
            exploredState.put(poll, state);
            trace.add(poll);
            if(poll.equals(goal)) {
                System.out.println("Total Nodes processed:"+trace.size());
                return reconstructPath(poll,parent);
            }
            List<String> list = nextStates(poll, n);
            for(String next: list) {
                if (!exploredState.containsKey(next) && !parent.containsKey(next)) {
                        parent.put(next, poll);
                        int d = poll.equals(initial) ? 0 : exploredState.get(poll).distanceToReach + 1;
                        State s = new State(poll, d);
                        exploredState.put(next,s);
                        pq.add(next);
                }
            }
            iterationCapped--;
        }
        if(iterationCapped==0){
            System.out.println("Exit for max iteration for:"+initial);
        }else{
            System.out.println("Not able to reach goal");
        }
        //empty means no path
        return Collections.emptyList();
    }

    public static void main(String[] args) {
        String targetState = "12345678b";
        for(Algorithm algo: Algorithm.values()) {
            System.out.println("******************* Running algorithm: "+algo+" *******************");
            int reachableIterationsCapped = 5;
            Set<String> dontRepeat = new HashSet<>();
            while (reachableIterationsCapped > 0) {
//            String startState = shuffleString(targetState);
                String startState = fixedInputs(reachableIterationsCapped - 1);
//            System.out.println("Start state: "+startState);
                boolean reachable = isReachable(decodeArray(startState, 3), decodeArray(targetState, 3));
//            System.out.println("Reachable: "+ reachable);
                if (reachable && !dontRepeat.contains(startState)) {
                    System.out.println("Start state: " + startState);
                    dontRepeat.add(startState);
                    reachableIterationsCapped--;
                    for (Heuristic h : Heuristic.values()) {
                        System.out.println("Using Heuristic:" + h.name());
                        List<String> solution = findSolution(startState, targetState, 3, h, algo);
                        System.out.println("Path size:" + (solution.size() - 1));
                        System.out.println("Path:"+String.join(" -> ", solution));
                    }
                }
            }
        }
    }
}