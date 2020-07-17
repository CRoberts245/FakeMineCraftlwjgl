//Cameron Roberts
//Professor Salloum
//Assignment#4

package criticalpathmethod;

import java.util.*; 

public class CriticalPathMethod {
    public static int maxTotalPath;
    

    public static String stringStructure = "%1$-10s %2$-5s %3$-5s %4$-5s %5$-5s %6$-5s %7$-10s\n";

    public static void main(String[] args) {
        //create hashset to hold nodes
        HashSet<Task> myTaskSet = new HashSet<Task>();
        
        //node creation
        Task end = new Task("End", 0);
        Task Z = new Task("Z", 10, end);
        Task Y = new Task("Y", 35, end);
        Task X = new Task("X", 3, Z, Y);
        Task W = new Task("W", 18, Y, X);
        Task begin = new Task("Begin", 0, W);
        
        //add nodes to hashset
        myTaskSet.add(end);
        myTaskSet.add(Z);
        myTaskSet.add(Y);
        myTaskSet.add(X);
        myTaskSet.add(W);
        myTaskSet.add(begin);
        //calc crit path 
        Task[] result = criticalPath(myTaskSet);
        //print result
        print(result);
        //output crit path
        System.out.println("Critical Path Nodes: ");
        for(int i = 0; i < result.length;i++){
                //exclude non crit nodes from output
                if(result[i].earlyStart == result[i].latestStart)
                System.out.print(result[i].nodeName+" ");
        }
        System.out.println("");
    }

        // class to hold task
    public static class Task {
        // the actual cost of the task
        public int cost;
        // the cost of the task along the critical path
        public int criticalCost;
        // a nodeName for the task for printing
        public String nodeName;
        // the earliest start
        public int earlyStart;
        // the earliest finish
        public int earlyFinish;
        // the latest start
        public int latestStart;
        // the latest finish
        public int latestFinish;
        // the tasks on which this task is dependant
        public HashSet<Task> dependencySet = new HashSet<Task>();

        public Task(String nodeName, int cost, Task... dependencySet) {
            this.nodeName = nodeName;
            this.cost = cost;
            for (Task t : dependencySet) {
                this.dependencySet.add(t);
            }
            this.earlyFinish = -1;
        }

        public void setLatest() {
            latestStart = maxTotalPath - criticalCost;
            latestFinish = latestStart + cost;
        }

        public String[] toStringArray() {
            String criticalCond = earlyStart == latestStart ? "Yes" : "No";
            String[] toString = { nodeName, earlyStart + "", earlyFinish + "", latestStart + "", latestFinish + "",
                    latestStart - earlyStart + "", criticalCond };
            return toString;
        }

        public boolean isDependent(Task t) {
            // is t a direct dependency?
            if (dependencySet.contains(t)) {
                return true;
            }
            // is t an indirect dependency
            for (Task dep : dependencySet) {
                if (dep.isDependent(t)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static Task[] criticalPath(Set<Task> tasks) {
        //finished nodes
        HashSet<Task> completed = new HashSet<Task>();
        //unfinished nodes
        HashSet<Task> leftOvers = new HashSet<Task>(tasks);

        while (!leftOvers.isEmpty()) {
            boolean progress = false;
            for (Iterator<Task> it = leftOvers.iterator(); it.hasNext();) {
                Task task = it.next();
                if (completed.containsAll(task.dependencySet)) {
                    int critical = 0;
                    for (Task t : task.dependencySet) {
                        if (t.criticalCost > critical) {
                            critical = t.criticalCost;
                        }
                    }
                    task.criticalCost = critical + task.cost;
                    completed.add(task);
                    it.remove();
                    //make sure that making progress
                    progress = true;
                }
            }
            // no prog = dependency
            if (!progress)
                throw new RuntimeException("Cyclic dependency, algorithm stopped!");
        }

        // get cost
        maxTotalPath(tasks);
        HashSet<Task> startNodes = initials(tasks);
        getEarlySF(startNodes);

        // get tasks
        Task[] returnTaskArray = completed.toArray(new Task[0]);

        Arrays.sort(returnTaskArray, new Comparator<Task>() {

            @Override
            public int compare(Task o1, Task o2) {
                return o1.nodeName.compareTo(o2.nodeName);
            }
        });

        return returnTaskArray;
    }

    public static void getEarlySF(HashSet<Task> initials) {
        for (Task initial : initials) {
            initial.earlyStart = 0;
            initial.earlyFinish = initial.cost;
            setEarlySF(initial);
        }
    }

    public static void setEarlySF(Task initial) {
        int completionTime = initial.earlyFinish;
        for (Task t : initial.dependencySet) {
            if (completionTime >= t.earlyStart) {
                t.earlyStart = completionTime;
                t.earlyFinish = completionTime + t.cost;
            }
            setEarlySF(t);
        }
    }

    public static HashSet<Task> initials(Set<Task> tasks) {
        HashSet<Task> leftOvers = new HashSet<Task>(tasks);
        for (Task t : tasks) {
            for (Task td : t.dependencySet) {
                leftOvers.remove(td);
            }
        }

        System.out.print("Start node: ");
        for (Task t : leftOvers)
            System.out.print(t.nodeName + " ");
        System.out.print("\n\n");
        return leftOvers;
    }

    public static void maxTotalPath(Set<Task> tasks) {
        int max = -1;
        for (Task t : tasks) {
            if (t.criticalCost > max)
                max = t.criticalCost;
        }
        maxTotalPath = max;
        System.out.println("Critical Total Path Cost: " + maxTotalPath);
        for (Task t : tasks) {
            t.setLatest();
        }
    }

    public static void print(Task[] tasks) {
        System.out.format(stringStructure, "Node", "ES", "EF", "LS", "LF", "Slack", "Critical?");
        for (Task t : tasks)
            System.out.format(stringStructure, (Object[]) t.toStringArray());
    }
}
