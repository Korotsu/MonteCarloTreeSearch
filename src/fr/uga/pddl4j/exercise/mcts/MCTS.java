package fr.uga.pddl4j.exercise.mcts;

import fr.uga.pddl4j.heuristics.state.StateHeuristic;
import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.parser.RequireKey;
import fr.uga.pddl4j.plan.Plan;
import fr.uga.pddl4j.plan.SequentialPlan;
import fr.uga.pddl4j.planners.AbstractPlanner;
import fr.uga.pddl4j.planners.Planner;
import fr.uga.pddl4j.planners.PlannerConfiguration;
import fr.uga.pddl4j.planners.ProblemNotSupportedException;
import fr.uga.pddl4j.problem.DefaultProblem;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.State;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.problem.operator.Condition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * The class is an Monte-carlo tree search planner able to solve an ADL problems.
 *
 * @author W. Nardone
 * @version 1.0 - 14/11/2023
 */
@CommandLine.Command(name = "MCTS",
    version = "MCTS 1.0",
    description = "Solves a specified planning problem using MCTS search strategy.",
    sortOptions = false,
    mixinStandardHelpOptions = true,
    headerHeading = "Usage:%n",
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription:%n%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n")
public class MCTS extends AbstractPlanner {

    /**
     * The class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(MCTS.class.getName());

    /**
     * The serial version unique ID.
     */
    private static final long serialVersionUID = 001;

    /**
     * Creates a new MCTS search planner with the default configuration.
     */
    public MCTS() {
        this(MCTS.getDefaultConfiguration());
    }

    /**
     * Creates a new MCTS search planner with a specified configuration.
     *
     * @param configuration the configuration of the planner.
     */
    public MCTS(final PlannerConfiguration configuration) {
        super();
        this.setConfiguration(configuration);
    }

    /**
     * Set the name of heuristic used by the planner to the solve a planning problem.
     *
     * @param heuristic the name of the heuristic.
     */
    @CommandLine.Option(names = {"-e", "--heuristic"}, defaultValue = "FAST_FORWARD",
        description = "Set the heuristic : AJUSTED_SUM, AJUSTED_SUM2, AJUSTED_SUM2M, COMBO, "
            + "MAX, FAST_FORWARD SET_LEVEL, SUM, SUM_MUTEX (preset: FAST_FORWARD)")
    public void setHeuristic(StateHeuristic.Name heuristic) {
        this.heuristic = heuristic;
    }

    /**
     * The name of the heuristic used by the planner.
     */
    private StateHeuristic.Name heuristic;

    /**
     * The HEURISTIC property used for planner configuration.
     */
    public static final String HEURISTIC_SETTING = "HEURISTIC";

    /**
     * The default value of the HEURISTIC property used for planner configuration.
     */
    public static final StateHeuristic.Name DEFAULT_HEURISTIC = StateHeuristic.Name.FAST_FORWARD;

    /**
     * Returns the name of the heuristic used by the planner to solve a planning problem.
     *
     * @return the name of the heuristic used by the planner to solve a planning problem.
     */
    public final StateHeuristic.Name getHeuristic() {
        return this.heuristic;
    }

    /**
     * Sets the weight of the heuristic.
     *
     * @param weight the weight of the heuristic. The weight must be greater than 0.
     * @throws IllegalArgumentException if the weight is strictly less than 0.
     */
    @CommandLine.Option(names = {"-w", "--weight"}, defaultValue = "1.0",
        paramLabel = "<weight>", description = "Set the weight of the heuristic (preset 1.0).")
    public void setHeuristicWeight(final double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight <= 0");
        }
        this.heuristicWeight = weight;
    }   

    /**
     * The weight of the heuristic.
     */
    private double heuristicWeight;

    /**
     * The WEIGHT_HEURISTIC property used for planner configuration.
     */
    public static final String WEIGHT_HEURISTIC_SETTING = "WEIGHT_HEURISTIC";

    /**
     * The default value of the WEIGHT_HEURISTIC property used for planner configuration.
     */
    public static final double DEFAULT_WEIGHT_HEURISTIC = 1.0;

    /**
     * Returns the weight of the heuristic.
     *
     * @return the weight of the heuristic.
     */
    public final double getHeuristicWeight() {
        return this.heuristicWeight;
    }

    /**
     * Set the maximum number of branch to explore by each step of MCTS.
     *
     * @param numWalk the maximum number of branch to explore by each step of MCTS.
     */
    @CommandLine.Option(names = {"-nW", "--numWalk"}, defaultValue = "2000",
        paramLabel = "<numWalk>", description = "Set the maximum number of branch to explore by each step of MCTS. (preset 2000)")
    public void setNumWalk(final int numWalk) {
        if (numWalk <= 0) {
            throw new IllegalArgumentException("NumWalk <= 0");
        }
        this.numWalk = numWalk;
    }

    /**
     * The maximum number of branch to explore by each step of MCTS.
     */
    private int numWalk;

    /**
     * The NUM_WALK property used for planner configuration.
     */
    public static final String NUM_WALK_SETTING = "NUM_WALK";

    /**
     * The default value of the NUM_WALK property used for planner configuration.
     */
    public static final int DEFAULT_NUM_WALK = 2000;

    /**
     * Returns maximum number of branch to explore by each step of MCTS.
     *
     * @return maximum number of branch to explore by each step of MCTS.
     */
    public final int getNumWalk() {
        return this.numWalk;
    }

    /**
     * Set the maximum length of a walk.
     *
     * @param lengthWalk the maximum length of a walk.
     */
    @CommandLine.Option(names = {"-lW", "--lengthWalk"}, defaultValue = "10",
        paramLabel = "<lengthWalk>", description = "Set the maximum length of a walk. (preset 10)")
    public void setLengthWalk(final int lengthWalk) {
        if (lengthWalk <= 0) {
            throw new IllegalArgumentException("LengthWalk <= 0");
        }
        this.lengthWalk = lengthWalk;
    }

    /**
     * The maximum length of a walk.
     */
    private int lengthWalk;

    /**
     * The LENGTH_WALK property used for planner configuration.
     */
    public static final String LENGTH_WALK_SETTING = "LENGTH_WALK";

    /**
     * The default value of the LENGTH_WALK property used for planner configuration.
     */
    public static final int DEFAULT_LENGTH_WALK = 10;

    /**
     * Returns the maximum length of a walk.
     *
     * @return the maximum length of a walk.
     */
    public final int getLengthWalk() {
        return this.lengthWalk;
    }

    /**
     * Set the maximum number of steps before starting to explore another branch of the MCTS.
     *
     * @param maxSteps the maximum number of steps before starting to explore another branch of the MCTS.
     */
    @CommandLine.Option(names = {"-mS", "--maxSteps"}, defaultValue = "7",
        paramLabel = "<maxSteps>", description = "Set the maximum number of steps before starting to explore another "
                                                    + "branch of the MCTS. (preset 7)")
    public void setMaxSteps(final int maxSteps) {
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("MaxSteps <= 0");
        }
        this.maxSteps = maxSteps;
    }

    /**
     * The maximum number of steps before starting to explore another branch of the MCTS.
     */
    private int maxSteps;

    /**
     * The NUM_WALK property used for planner configuration.
     */
    public static final String MAX_STEPS_SETTING = "NUM_WALK";

    /**
     * The default value of the NUM_WALK property used for planner configuration.
     */
    public static final int DEFAULT_MAX_STEPS = 7;

    /**
     * Returns maximum number of steps before starting to explore another branch of the MCTS.
     *
     * @return maximum number of steps before starting to explore another branch of the MCTS.
     */
    public final int getMaxSteps() {
        return this.maxSteps;
    }

    /**
     * Instantiates the planning problem from a parsed problem.
     *
     * @param problem the problem to instantiate.
     * @return the instantiated planning problem or null if the problem cannot be instantiated.
     */
    @Override
    public Problem instantiate(DefaultParsedProblem problem) {
        final Problem pb = new DefaultProblem(problem);
        pb.instantiate();
        return pb;
    }

    /**
     * Search a solution plan to a specified domain and problem using MCTS.
     *
     * @param problem the problem to solve.
     * @return the plan found or null if no plan was found.
     */
    @Override
    public Plan solve(final Problem problem) throws ProblemNotSupportedException{
        LOGGER.info("* Starting MCTS search \n");
        // Search a solution
        final long begin = System.currentTimeMillis();
        final Plan plan = this.mcts(problem);
        final long end = System.currentTimeMillis();
        // If a plan is found update the statistics of the planner and log search information
        if (plan != null) {
            LOGGER.info("* MCTS search succeeded\n");
            this.getStatistics().setTimeToSearch(end - begin);
        } else {
            LOGGER.info("* MCTS search failed\n");
        }
        // Return the plan found or null if the search fails.
        return plan;
    }

    /**
     * The main method of the <code>MCTS</code> planner.
     *
     * @param args the arguments of the command line.
     */
    public static void main(String[] args) {
        try {
            final MCTS planner = new MCTS();
            CommandLine cmd = new CommandLine(planner);
            cmd.execute(args);
        } catch (IllegalArgumentException e) {
            LOGGER.fatal(e.getMessage());
        }
    }

    /**
     * Returns if a specified problem is supported by the planner. Just ADL problem can be solved by this planner.
     *
     * @param problem the problem to test.
     * @return <code>true</code> if the problem is supported <code>false</code> otherwise.
     */
    @Override
    public boolean isSupported(Problem problem) {
        return (problem.getRequirements().contains(RequireKey.ACTION_COSTS)
            || problem.getRequirements().contains(RequireKey.CONSTRAINTS)
            || problem.getRequirements().contains(RequireKey.CONTINOUS_EFFECTS)
            || problem.getRequirements().contains(RequireKey.DERIVED_PREDICATES)
            || problem.getRequirements().contains(RequireKey.DURATIVE_ACTIONS)
            || problem.getRequirements().contains(RequireKey.DURATION_INEQUALITIES)
            || problem.getRequirements().contains(RequireKey.FLUENTS)
            || problem.getRequirements().contains(RequireKey.GOAL_UTILITIES)
            || problem.getRequirements().contains(RequireKey.METHOD_CONSTRAINTS)
            || problem.getRequirements().contains(RequireKey.NUMERIC_FLUENTS)
            || problem.getRequirements().contains(RequireKey.OBJECT_FLUENTS)
            || problem.getRequirements().contains(RequireKey.PREFERENCES)
            || problem.getRequirements().contains(RequireKey.TIMED_INITIAL_LITERALS)
            || problem.getRequirements().contains(RequireKey.HIERARCHY))
            ? false : true;
    }

    /**
     * Search a solution plan for a planning problem using an MCTS search strategy.
     *
     * @param problem the problem to solve.
     * @return a plan solution for the problem or null if there is no solution.
     * @throws ProblemNotSupportedException if the problem to solve is not supported by the planner.
     */
    public Plan mcts(Problem problem) throws ProblemNotSupportedException {
        // Check if the problem is supported by the planner.
        if (!this.isSupported(problem)) {
            throw new ProblemNotSupportedException("Problem not supported");
        }

        // First we create an instance of the heuristic to use to guide the search.
        final StateHeuristic heuristic = StateHeuristic.getInstance(this.getHeuristic(), problem);

        // We get the initial state from the planning problem.
        final State initialState = new State(problem.getInitialState());

        // Get the goal of the problem.
        final Condition goal = problem.getGoal();

        // We create the root node of the tree search
        final Node root = new Node(initialState, null, -1, 0, heuristic.estimate(initialState, goal));

        // Initialize the current state, the minimum heuristic value, the goal of the problem, the counter and the available actions.
        int counter = 0;
        Node currentLoopState = root; 
        double hmin = root.getHeuristic();
        List<Action> availableActions = problem.getActions();
        
        // Loop until we find a solution to the problem.
        while (!currentLoopState.satisfy(goal)) {
            // Reset the current state and counter if we do too much steps or the state is in a dead-end.
            if (counter > this.getMaxSteps() || this.applicableActions(currentLoopState, availableActions).isEmpty()) {
                currentLoopState = root;
                counter = 0;
            }

            // Execute the pureRandomWalks for the current state.
            currentLoopState = pureRandomWalks(currentLoopState, heuristic, goal, availableActions);

            // If the path we evaluate is worthier than the last best one then we replace it and reset the counter.
            if (currentLoopState.getHeuristic() < hmin) {
                hmin = currentLoopState.getHeuristic();
                counter = 0;
            }
            else
                counter++;
        }

        // Finally, we return the search computed or null if no search was found
        return this.extractPlan(currentLoopState, problem);
    }

    /**
     * Explore new substates to find a worthy state to continue the tree with using pure random walks.
     *
     * @param currentState the actual state of the MCTS we need to explore from.
     * @param heuristic the heuristic function to evaluate the state worth.
     * @param goal the goal of the problem.
     * @param actions the actions available from the problem.
     * @return a state found with the minimum heuristic value or the input state if nothing better was found.
     */
    public Node pureRandomWalks(Node currentState, StateHeuristic heuristic, Condition goal, List<Action> actions) {
        // We initialize the minimum heuristic and state value as well as the current state.
        double hmin = Double.MAX_VALUE;
        Node smin = null;
        Node currentLoopState = null;

        // We initialize the random number generator used for the selection of actions.
        Random randomGenerator = new Random();
        
        // We loop until we reach the maximum number of walk that we defined at the start of the MCTS.
        for (int i = 0; i < this.getNumWalk(); i++) {
            //We reset the current state to be at the inital state of the function to explore a new branch of the tree.
            currentLoopState = currentState;
            // We loop until we reach the maximum length of a walk that we defined at the start of the MCTS.
            for (int j = 0; j < this.getLengthWalk(); j++) {
                // We get the all the applicables actions of the problem at this state.
                List<Integer> applicableActions = this.applicableActions(currentLoopState, actions);
                
                // Check if their is any applicable actions for this state.
                if (applicableActions == null || applicableActions.isEmpty())
                    break;

                // We uniformly random select an action from the applicable action pool.
                int choosenAction = applicableActions.get(randomGenerator.nextInt(applicableActions.size()));
                
                // We create the new node of the tree from the current one.
                currentLoopState = new Node(currentLoopState, currentLoopState, choosenAction,
                                            currentLoopState.getCost() + 1, currentLoopState.getHeuristic());

                // We apply the effect of the action and update the heuristic acordingly.
                currentLoopState.apply(actions.get(choosenAction).getConditionalEffects());
                currentLoopState.setHeuristic(heuristic.estimate(currentLoopState, goal));

                // If the new state satisfy the goal condition then we have found a solution.
                if (currentLoopState.satisfy(goal))
                    return currentLoopState;
            }

            // If the path we evaluate is worthier than the last best one then we replace it.
            if (currentLoopState.getHeuristic() < hmin) {
                smin = currentLoopState;
                hmin = currentLoopState.getHeuristic();
            }
        }

        // We return the best state we found if any, otherwise we return the inital state of the function.
        return (smin != null) ? smin : currentState;
    }

    /**
     * Evaluate applicable actions in an action list for a specific state.
     *
     * @param evaluatedState the state that we need to evaluate applicable actions from.
     * @param actionPool the pool of actions to evaluate.
     * @return a list of actions applicable to the evaluated state as ID of the base action pool.
     */
    public List<Integer> applicableActions(State evaluatedState, List<Action> actionPool) {
        
        // Check if the given action pool is useable.
        if (actionPool == null || actionPool.isEmpty())
            return null;
        
        // Filter non applicable actions in the action pool in the result pool list as Integer.
        List<Integer> resultPool = new ArrayList<Integer>();
        for (int i = 0; i < actionPool.size(); i++) {
            if (actionPool.get(i).isApplicable(evaluatedState))
                resultPool.add(i);
        }

        // Return the list of actions applicable to the evaluated state.
        return resultPool;
    }

    /**
     * Extracts a search from a specified node.
     *
     * @param node    the node.
     * @param problem the problem.
     * @return the search extracted from the specified node.
     */
    private Plan extractPlan(final Node node, final Problem problem) {
        Node n = node;
        
        // If the node has no action it mean that no plan was created.
        if (n.getAction() == -1)
            return null;

        final Plan plan = new SequentialPlan(); 
        while (n.getAction() != -1) {
            final Action a = problem.getActions().get(n.getAction());
            plan.add(0, a);
            n = n.getParent();
        }
        return plan;
    }
    
    /**
     * Returns the configuration of the planner.
     *
     * @return the configuration of the planner.
     */
    @Override
    public PlannerConfiguration getConfiguration() {
        final PlannerConfiguration config = super.getConfiguration();
        config.setProperty(MCTS.HEURISTIC_SETTING, this.getHeuristic().toString());
        config.setProperty(MCTS.WEIGHT_HEURISTIC_SETTING, Double.toString(this.getHeuristicWeight()));
        config.setProperty(MCTS.NUM_WALK_SETTING, Integer.toString(this.getNumWalk()));
        config.setProperty(MCTS.LENGTH_WALK_SETTING, Integer.toString(this.getLengthWalk()));
        config.setProperty(MCTS.MAX_STEPS_SETTING, Integer.toString(this.getMaxSteps()));
        return config;
    }

    /**
     * Sets the configuration of the planner. If a planner setting is not defined in
     * the specified configuration, the setting is initialized with its default value.
     *
     * @param configuration the configuration to set.
     */
    @Override
    public void setConfiguration(final PlannerConfiguration configuration) {
        super.setConfiguration(configuration);
        if (configuration.getProperty(MCTS.WEIGHT_HEURISTIC_SETTING) == null) {
            this.setHeuristicWeight(MCTS.DEFAULT_WEIGHT_HEURISTIC);
        } else {
            this.setHeuristicWeight(Double.parseDouble(configuration.getProperty(
                MCTS.WEIGHT_HEURISTIC_SETTING)));
        }
        if (configuration.getProperty(MCTS.HEURISTIC_SETTING) == null) {
            this.setHeuristic(MCTS.DEFAULT_HEURISTIC);
        } else {
            this.setHeuristic(StateHeuristic.Name.valueOf(configuration.getProperty(
                MCTS.HEURISTIC_SETTING)));
        }
        if (configuration.getProperty(MCTS.NUM_WALK_SETTING) == null) {
            this.setNumWalk(MCTS.DEFAULT_NUM_WALK);
        } else {
            this.setNumWalk(Integer.parseInt(configuration.getProperty(
                MCTS.NUM_WALK_SETTING)));
        }
        if (configuration.getProperty(MCTS.LENGTH_WALK_SETTING) == null) {
            this.setLengthWalk(MCTS.DEFAULT_LENGTH_WALK);
        } else {
            this.setLengthWalk(Integer.parseInt(configuration.getProperty(
                MCTS.LENGTH_WALK_SETTING)));
        }
        if (configuration.getProperty(MCTS.MAX_STEPS_SETTING) == null) {
            this.setMaxSteps(MCTS.DEFAULT_MAX_STEPS);
        } else {
            this.setMaxSteps(Integer.parseInt(configuration.getProperty(
                MCTS.MAX_STEPS_SETTING)));
        }
    }

    /**
     * Returns the default arguments of the planner.
     * 
     * @return the default arguments of the planner.
     * @see PlannerConfiguration
     */
    public static PlannerConfiguration getDefaultConfiguration() {
        PlannerConfiguration config = Planner.getDefaultConfiguration();
        config.setProperty(MCTS.HEURISTIC_SETTING, MCTS.DEFAULT_HEURISTIC.toString());
        config.setProperty(MCTS.WEIGHT_HEURISTIC_SETTING, Double.toString(MCTS.DEFAULT_WEIGHT_HEURISTIC));
        config.setProperty(MCTS.NUM_WALK_SETTING, Integer.toString(MCTS.DEFAULT_NUM_WALK));
        config.setProperty(MCTS.LENGTH_WALK_SETTING, Integer.toString(MCTS.DEFAULT_LENGTH_WALK));
        config.setProperty(MCTS.MAX_STEPS_SETTING, Integer.toString(MCTS.DEFAULT_MAX_STEPS));
        return config;
    }

    /**
     * Checks the planner configuration and returns if the configuration is valid.
     * A configuration is valid if :
     * (1) the domain and the problem files exist and can be read, 
     * (2) the timeout is greater than 0, 
     * (3) the weight of the heuristic is greater than 0, 
     * (4) the heuristic is a not null,
     * (5) the number of walk is greater than 0,
     * (6) the length of walk is greater than 0,
     * (7) the maximum number of steps is greater than 0.
     *
     * @return <code>true</code> if the configuration is valid <code>false</code> otherwise.
     */
    public boolean hasValidConfiguration() {
        return super.hasValidConfiguration()
            && this.getHeuristicWeight() > 0.0
            && this.getHeuristic() != null
            && this.getNumWalk() > 0
            && this.getLengthWalk() > 0
            && this.getMaxSteps() > 0;
    }
}