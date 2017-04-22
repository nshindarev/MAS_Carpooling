package dreamteam.carpooling.appl.DriverBehaviours;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Получение предложений от пассажиров
 */
public class ProposalsReceiverBehaviour extends SimpleBehaviour {

    private long timeOut, wakeupTime;
    private boolean finished;

    private DriverFSMBehaviour myParentFSM; // Экземпляр конечного автомата водителя

    private ACLMessage msg;
    private int messagesReceived;

    MessageTemplate template;

    public ACLMessage getMessage() { return msg; }

    public ProposalsReceiverBehaviour(Agent a, int millis) {
        super(a);
        timeOut = millis;
    }

    public void onStart() {
        myParentFSM = (DriverFSMBehaviour) getParent();
        template = new MessageTemplate((MessageTemplate.MatchExpression) aclMessage ->
                aclMessage.getPerformative() == (ACLMessage.PROPOSE)); // Шаблон для предложений
        wakeupTime = (timeOut<0 ? Long.MAX_VALUE
                :System.currentTimeMillis() + timeOut);
    }

    @Override
    public boolean done () {
        return finished;
    }

    @Override
    public void action()
    {
        msg = myAgent.receive(template);

        if( msg != null) {
            finished = true;
            handle( msg );
            return;
        }
        long dt = wakeupTime - System.currentTimeMillis();
        if ( dt > 0 )
            block(dt);
        else {
            finished = true;
            handle( msg );
        }
    }

    public void handle(ACLMessage m) {

        if (m == null) {
            // TODO: Время вышло, сообщения нет
        } else {
            // TODO: Обрабатываем сообщение
        }

    }

    public void reset() {
        msg = null;
        finished = false;
        super.reset();
    }

    public void reset(int dt) {
        timeOut= dt;
        reset();
    }
}

