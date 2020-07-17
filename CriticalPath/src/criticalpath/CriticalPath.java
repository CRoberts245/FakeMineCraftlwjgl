//Cameron Roberts
//Professor Salloum
//Assignment#4

package criticalpath;
import java.util.*; 

public class CriticalPath {
    public static int maxTotalPath;
    

    public static String format = "%1$-10s %2$-5s %3$-5s %4$-5s %5$-5s %6$-5s %7$-10s\n";

    public static void main(String[] args) {
        //create hashset to hold nodes
        HashSet<Task> myTaskSet = new HashSet<Task>();
        
        //node creation
        Task end = new Task("End", 0);
        Task Z = new Task("Z", 20, end);
        Task Y = new Task("Y", 35, end);
        Task X = new Task("X", 40, Z, Y);
        Task W = new Task("W", 28, Y, X);
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
        // tasks whose critical cost has been calculated
        HashSet<Task> completed = new HashSet<Task>();
        // tasks whose critical cost needs to be calculated
        HashSet<Task> leftOvers = new HashSet<Task>(tasks);

        // Backflow algorithm
        // while there are tasks whose critical cost isn't calculated.
        while (!leftOvers.isEmpty()) {
            boolean progress = false;

            // find a new task to calculate
            for (Iterator<Task> it = leftOvers.iterator(); it.hasNext();) {
                Task task = it.next();
                if (completed.containsAll(task.dependencySet)) {
                    // all dependencySet calculated, critical cost is max
                    // dependency
                    // critical cost, plus our cost
                    int critical = 0;
                    for (Task t : task.dependencySet) {
                        if (t.criticalCost > critical) {
                            critical = t.criticalCost;
                        }
                    }
                    task.criticalCost = critical + task.cost;
                    // set task as calculated an remove
                    completed.add(task);
                    it.remove();
                    // note we are making progress
                    progress = true;
                }
            }
            // If we haven't made any progress then a cycle must exist in
            // the graph and we wont be able to calculate the critical path
            if (!progress)
                throw new RuntimeException("Cyclic dependency, algorithm stopped!");
        }

        // get the cost
        maxTotalPath(tasks);
        HashSet<Task> startNodes = initials(tasks);
        getEarlySF(startNodes);

        // get the tasks
        Task[] ret = completed.toArray(new Task[0]);
        // create a priority list
        Arrays.sort(ret, new Comparator<Task>() {

            @Override
            public int compare(Task o1, Task o2) {
                return o1.nodeName.compareTo(o2.nodeName);
            }
        });

        return ret;
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

        System.out.print("Initial nodes: ");
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
        System.out.println("Critical Total Path Cost): " + maxTotalPath);
        for (Task t : tasks) {
            t.setLatest();
        }
    }

    public static void print(Task[] tasks) {
        System.out.format(format, "Node", "ES", "EF", "LS", "LF", "Slack", "Critical?");
        for (Task t : tasks)
            System.out.format(format, (Object[]) t.toStringArray());
    }
}
