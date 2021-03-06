package dreamteam.carpooling.appl.Util;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Агент собирает статистику.
 */
public class StatsAgent extends Agent {

    public static final Logger logger = LoggerFactory.getLogger(StatsAgent.class);

    private int agentsCounter, agentsAmount;
    private double totalMileage, startTotalMileage;
    private MyCityGraph<String, MyWeightedEdge> city;
    private HashMap<String, String[]> routes;
    private HashMap<String, String[]> passengers;
    private ArrayList<String> alonePassengers;

    @Override
    protected void setup() {
        super.setup();

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            agentsAmount = Integer.parseInt(args[0].toString());
        }

        agentsCounter = 0;
        totalMileage = 0;
        startTotalMileage = 0;
        city = City.getCity();
        routes = new HashMap<>();
        passengers = new HashMap<>();
        alonePassengers = new ArrayList<>();

        addBehaviour(new SimpleBehaviour() {

            public MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

            @Override
            public void action() {
                ACLMessage msg = myAgent.receive(template);
                if (msg != null) {
                    String driver = msg.getSender().getLocalName();

                    if (msg.getContent().equals(Conversation.NOT_FOUND_DRIVER)) {
                        agentsCounter++;
                        alonePassengers.add(msg.getSender().getLocalName());
                        return;
                    }

                    if (msg.getContent().contains("sc")) {
                        startTotalMileage += Double.valueOf(msg.getContent().substring(2, msg.getContent().length()));
                        return;
                    }

                    // Водитель пересылает список пассажиров и маршрут
                    ArrayList<String> passengers = new ArrayList<>();
                    ArrayList<String> route = new ArrayList<>();
                    for (String s : msg.getContent().split(",")) {
                        if (s.contains("agent")) {
                            passengers.add(s);
                        } else {
                            route.add(s);
                        }
                    }

                    // Считаем агентов (водителя + всех его пассажиров)
                    agentsCounter += 1 + passengers.size();

                    // Сохраняем пассажиров водителя
                    StatsAgent.this.passengers.put(driver, passengers.toArray(new String[passengers.size()]));

                    // Сохраняем маршрут
                    routes.put(driver, route.toArray(new String[route.size()]));

                    // Считаем длину
                    for (int i = 1; i < route.size(); i++) {
                        try {
                            totalMileage += city.getEdge(route.get(i - 1), route.get(i)).get_weight();
                        }
                        catch (NullPointerException ex){
                            StatsAgent.logger.error(ex.getMessage());
                        }
                    }

                }
                else {
                    block();
                }
            }

            @Override
            public boolean done() {
                if (agentsCounter > agentsAmount) {
                    StatsAgent.logger.error("Error: too many agent counted ({} of {})",
                            agentsCounter, agentsAmount);
                    myAgent.doDelete();
                    return false;
                }
                return agentsCounter == agentsAmount;
            }

            @Override
            public int onEnd() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                printLog();
                return super.onEnd();
            }
        });
    }

    private void printLog() {
        StatsAgent.logger.info("Summary:");

        routes.forEach((driver, route) -> {

            StringBuilder s = new StringBuilder(driver);
            s.append(": route ");

            for (int i = 0; i < route.length; i++) {
                s.append(route[i]).append(i == route.length - 1 ?
                                        ", passengers: " :
                                        " -> ");
            }

            String[] passengers = this.passengers.get(driver);
            if (passengers.length == 0) {
                s.append("none");
            }
            for (int i = 0; i < passengers.length; i++) {
                s.append(passengers[i]).append(i == route.length - 1 ?
                                                    "" :
                                                    ", ");
            }

            StatsAgent.logger.info(s.toString());

        });

        if (alonePassengers.size() > 0) {
            final String[] s = {""};
            alonePassengers.forEach(s1 -> s[0] = s[0].concat(s1).concat(", "));
            StatsAgent.logger.info("Alone passengers: {}", s[0].substring(0, s[0].length() - 2));
        }

        StatsAgent.logger.info("Start total mileage: {}", startTotalMileage);
        StatsAgent.logger.info("Total mileage: {}", totalMileage);
    }
}
