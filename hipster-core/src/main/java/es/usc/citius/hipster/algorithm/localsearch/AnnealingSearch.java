package es.usc.citius.hipster.algorithm.localsearch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import es.usc.citius.hipster.algorithm.Algorithm;
import es.usc.citius.hipster.model.HeuristicNode;
import es.usc.citius.hipster.model.Node;
import es.usc.citius.hipster.model.function.NodeExpander;

/**
 * Implementation of the simulated annealing search that is a probabilistic
 * technique for approximating the global optimum of a given function. It starts
 * the exploration from a random point as a global optimum and selects one of
 * its neighbors with a neighboring function. The neighbor will become the new
 * optimum if its associated cost is lower or if the acceptance probability
 * function returns a probability greater than a random number. The probability
 * function takes as an input the cost of the current selected node, the cost of
 * its randomly selected neighbour and the current temperature. The higher the
 * cost of the neighbour is or the lower the temperature is, the more unlikely
 * it is that the neighbour becomes the new optimum. The process continues until
 * the temperature is below a given threshold. The temperature decreases at each
 * iteration according to a geometric cooling schedule that has two parameters
 * alpha and temperature min. The main idea of this algorithm is to avoid to be
 * "trapped" in a bad local optimum by exploring more deeply the state space by
 * looking at states whose cost is not optimum but that may have interesting
 * neighbours. A user can adjusted the algorithm by tuning the alpha coefficient
 * (default 0.9) or the min temperature (0.00001) or by providing his own
 * implementation of the acceptance probability function (default: exp((old
 * score - new score) / current temperature)) or the neighbouring function
 * (random selection by default). Note: costs are Double in this implementation
 * and have no type parameters.
 * 
 * see <a href="https://en.wikipedia.org/wiki/Simulated_annealing">in
 * Wikipedia</a> and <a href="http://katrinaeg.com/simulated-annealing.html">in
 * annealing search</a> for more details.
 * 
 * @param <A>
 *            class defining the action
 * @param <S>
 *            class defining the state
 * @param <C>
 *            class defining the cost, must implement
 *            {@link java.lang.Comparable}
 * @param <N>
 *            type of the nodes
 * 
 * @author Christophe Moins <
 *         <a href="mailto:christophe.moins@yahoo.fr">christophe.moins@yahoo.fr
 *         </a>>
 */
package es.usc.citius.hipster.algorithm.localsearch;

import es.usc.citius.hipster.algorithm.Algorithm;
import es.usc.citius.hipster.model.HeuristicNode;
import es.usc.citius.hipster.model.Node;
import es.usc.citius.hipster.model.function.NodeExpander;
import java.util.*;

public class AnnealingSearch<A, S, N extends HeuristicNode<A, S, Double, N>> extends Algorithm<A, S, N> {

    private static final Double DEFAULT_ALPHA = 0.9;
    private static final Double DEFAULT_MIN_TEMP = 0.00001;
    private static final Double START_TEMP = 1.;

    private final N initialNode;
    private final Double alpha;
    private final Double minTemp;
    private final AcceptanceProbability acceptanceProbability;
    private final SuccessorFinder<A, S, N> successorFinder;
    private final NodeExpander<A, S, N> nodeExpander;
    
    // Solución incidencia 4: Reutilizamos una única instancia de Random
    private final Random random = new Random();

    public AnnealingSearch(N initialNode, NodeExpander<A, S, N> nodeExpander, Double alpha, Double minTemp,
                          AcceptanceProbability acceptanceProbability, SuccessorFinder<A, S, N> successorFinder) {
        
        // Validaciones obligatorias
        if (initialNode == null) throw new IllegalArgumentException("Provide a valid initial node");
        if (nodeExpander == null) throw new IllegalArgumentException("Provide a valid node expander");

        this.initialNode = initialNode;
        this.nodeExpander = nodeExpander;
        
        // Solución incidencia 3: Reducción de complejidad cognitiva delegando en métodos
        this.alpha = configureAlpha(alpha);
        this.minTemp = configureMinTemp(minTemp);
        this.acceptanceProbability = configureAcceptanceProbability(acceptanceProbability);
        this.successorFinder = configureSuccessorFinder(successorFinder);
    }

    private Double configureAlpha(Double alpha) {
        if (alpha != null) {
            if (alpha <= 0. || alpha >= 1.0) throw new IllegalArgumentException("alpha must be between 0. and 1.");
            return alpha;
        }
        return DEFAULT_ALPHA;
    }

    private Double configureMinTemp(Double minTemp) {
        if (minTemp != null) {
            if (minTemp < 0. || minTemp > 1.) throw new IllegalArgumentException("Minimum temperature must be between 0. and 1.");
            return minTemp;
        }
        return DEFAULT_MIN_TEMP;
    }

    private AcceptanceProbability configureAcceptanceProbability(AcceptanceProbability ap) {
        if (ap != null) return ap;
        return (oldScore, newScore, temp) -> (newScore < oldScore ? 1 : Math.exp((oldScore - newScore) / temp));
    }

    private SuccessorFinder<A, S, N> configureSuccessorFinder(SuccessorFinder<A, S, N> sf) {
        if (sf != null) return sf;
        return (node, expander) -> {
            List<N> successors = new ArrayList<>();
            for (N successor : expander.expand(node)) {
                successors.add(successor);
            }
            // Usamos el Random de la clase principal
            return successors.get(Math.abs(random.nextInt()) % successors.size());
        };
    }

    @Override
    public ASIterator iterator() {
        return new ASIterator();
    }

    public class ASIterator implements Iterator<N> {
        private final Queue<N> queue = new LinkedList<>();
        private Double bestScore;
        private Double curTemp = START_TEMP;

        private ASIterator() {
            bestScore = initialNode.getEstimation();
            queue.add(initialNode);
        }

        @Override
        public boolean hasNext() {
            return !queue.isEmpty();
        }

        @Override
        public N next() {
            if (!hasNext()) throw new NoSuchElementException();
            N currentNode = this.queue.poll();
            if (curTemp > minTemp) {
                processTemperatureStep(currentNode);
            }
            return currentNode;
        }

        private void processTemperatureStep(N currentNode) {
            N newNode = null;
            for (int i = 0; i < 100; i++) {
                N randSuccessor = successorFinder.estimate(currentNode, nodeExpander);
                Double score = randSuccessor.getScore();
                if (acceptanceProbability.compute(bestScore, score, curTemp) > Math.random()) {
                    newNode = randSuccessor;
                    bestScore = score;
                }
            }
            queue.add(newNode != null ? newNode : currentNode);
            curTemp *= alpha;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public interface AcceptanceProbability {
        Double compute(Double oldScore, Double newScore, Double temp);
    }

    public interface SuccessorFinder<A, S, N extends Node<A, S, N>> {
        N estimate(N node, NodeExpander<A, S, N> nodeExpander);
    }
}


