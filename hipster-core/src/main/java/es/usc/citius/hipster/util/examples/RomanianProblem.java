package es.usc.citius.hipster.util.examples;

import es.usc.citius.hipster.graph.GraphBuilder;
import es.usc.citius.hipster.model.function.HeuristicFunction;
import es.usc.citius.hipster.graph.HipsterGraph;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Definition of the states, transitions, costs and heuristics for the Romania Problem
 * as described in http://www.pearsonhighered.com/assets/hip/us/hip_us_pearsonhighered/samplechapter/0136042597.pdf.
 * </p>
 *
 * @author Adrián González Sieira <<a href="adrian.gonzalez@usc.es">adrian.gonzalez@usc.es</a>>
 * @author Pablo Rodríguez Mier <<a href="mailto:pablo.rodriguez.mier@usc.es">pablo.rodriguez.mier@usc.es</a>>
 */
public class RomanianProblem {

    /**
     * Enum with all the cities of the problem.
     * Solución incidencia 5: Nombres en MAYÚSCULAS según estándar Java (java:S115).
     */
   public enum City {
        ARAD, BUCHAREST, CRAIOVA, DROBETA, EFORIE, FAGARAS, GIURGIU,
        HIRSOVA, IASI, LUGOJ, MEHADIA, NEAMT, ORADEA, PITESTI, RIMNICU_VILCEA,
        SIBIU, TIMISOARA, URZICENI, VASLUI, ZERIND;
    }

    private static final Map<City, Double> heuristicMap = new HashMap<City, Double>();
    private static final HipsterGraph<City,Double> graph;

    /**
     * Define the heuristic values of the problem and create the graph.
     */
    static {
        heuristicMap.put(City.ORADEA, 380d);
        heuristicMap.put(City.ZERIND, 374d);
        heuristicMap.put(City.ARAD, 366d);
        heuristicMap.put(City.TIMISOARA, 329d);
        heuristicMap.put(City.LUGOJ, 244d);
        heuristicMap.put(City.MEHADIA, 241d);
        heuristicMap.put(City.DROBETA, 242d);
        heuristicMap.put(City.CRAIOVA, 160d);
        heuristicMap.put(City.RIMNICU_VILCEA, 193d);
        heuristicMap.put(City.PITESTI, 100d);
        heuristicMap.put(City.SIBIU, 253d);
        heuristicMap.put(City.FAGARAS, 176d);
        heuristicMap.put(City.GIURGIU, 77d);
        heuristicMap.put(City.URZICENI, 80d);
        heuristicMap.put(City.HIRSOVA, 151d);
        heuristicMap.put(City.EFORIE, 161d);
        heuristicMap.put(City.VASLUI, 199d);
        heuristicMap.put(City.IASI, 226d);
        heuristicMap.put(City.NEAMT, 234d);
        heuristicMap.put(City.BUCHAREST, 0d);

        graph = GraphBuilder.<City,Double>create()
                .connect(City.ARAD).to(City.ZERIND).withEdge(75d)
                .connect(City.ARAD).to(City.TIMISOARA).withEdge(118d)
                .connect(City.ARAD).to(City.SIBIU).withEdge(140d)
                .connect(City.BUCHAREST).to(City.GIURGIU).withEdge(90d)
                .connect(City.BUCHAREST).to(City.URZICENI).withEdge(85d)
                .connect(City.BUCHAREST).to(City.FAGARAS).withEdge(211d)
                .connect(City.BUCHAREST).to(City.PITESTI).withEdge(101d)
                .connect(City.CRAIOVA).to(City.DROBETA).withEdge(120d)
                .connect(City.CRAIOVA).to(City.RIMNICU_VILCEA).withEdge(146d)
                .connect(City.CRAIOVA).to(City.PITESTI).withEdge(138d)
                .connect(City.DROBETA).to(City.MEHADIA).withEdge(75d)
                .connect(City.EFORIE).to(City.HIRSOVA).withEdge(86d)
                .connect(City.FAGARAS).to(City.SIBIU).withEdge(99d)
                .connect(City.HIRSOVA).to(City.URZICENI).withEdge(98d)
                .connect(City.IASI).to(City.NEAMT).withEdge(87d)
                .connect(City.IASI).to(City.VASLUI).withEdge(92d)
                .connect(City.LUGOJ).to(City.TIMISOARA).withEdge(111d)
                .connect(City.LUGOJ).to(City.MEHADIA).withEdge(70d)
                .connect(City.ORADEA).to(City.ZERIND).withEdge(71d)
                .connect(City.ORADEA).to(City.SIBIU).withEdge(151d)
                .connect(City.PITESTI).to(City.RIMNICU_VILCEA).withEdge(97d)
                .connect(City.RIMNICU_VILCEA).to(City.SIBIU).withEdge(80d)
                .connect(City.URZICENI).to(City.VASLUI).withEdge(142d)
                .createUndirectedGraph();
    }

    public static HipsterGraph<City, Double> graph(){
        return graph;
    }

    public static Map<City, Double> heuristics(){
        return heuristicMap;
    }

    public static HeuristicFunction<City, Double> heuristicFunction(){
        return new HeuristicFunction<City, Double>() {
            @Override
            public Double estimate(City state) {
                return heuristics().get(state);
            }
        };
    }
}
