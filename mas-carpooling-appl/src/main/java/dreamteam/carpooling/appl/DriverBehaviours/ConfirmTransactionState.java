package dreamteam.carpooling.appl.DriverBehaviours;

import dreamteam.carpooling.appl.CitizenAgent;
import dreamteam.carpooling.appl.Util.Conversation;
import dreamteam.carpooling.appl.Util.Offer;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Подтверждение транзакции
 */
public class ConfirmTransactionState extends OneShotBehaviour {

    private DriverFSMBehaviour myParentFSM;

    @Override
    public void action() {
        List<Offer> not_best_offer = new LinkedList<>();
        myParentFSM = (DriverFSMBehaviour) getParent();

        // для каждого предложения из best_offers генерим Agree

        for (Offer offer:
                myParentFSM.myCitizenAgent.best_offer) {

            ACLMessage reply = offer.message.createReply();
            reply.setPerformative(ACLMessage.AGREE);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MILLISECOND, Conversation.REPLY_TIME);
            reply.setReplyByDate(calendar.getTime());

            reply.setContent(Conversation.CONTENT_STUB);

            myParentFSM.getAgent().send(reply);

            CitizenAgent.logger.info("{} driver AGREED driving with proposal from {}",
                    myAgent.getLocalName(),
                    offer.message.getSender().getLocalName());

        }

        // убираем из пула все лучшие предложения, на которые уже ответили
        for (Offer offer:
                myParentFSM.myCitizenAgent.getOffersPool()) {
            not_best_offer.add(offer);
        }
        for (Offer best_offer:
                myParentFSM.myCitizenAgent.getBestOffer()) {
            for (Offer simple_offer:
                    not_best_offer) {

                if (best_offer.id.getName().equals(simple_offer.id.getName())){
                    not_best_offer.remove(simple_offer);
                }

            }

        }

        // для каждого из оставшихся предложений из пула генерим REJECT
        for (Offer offer:
                not_best_offer) {

            ACLMessage reply = offer.message.createReply();
            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MILLISECOND, Conversation.REPLY_TIME);
            reply.setReplyByDate(calendar.getTime());

            reply.setContent(Conversation.NO_SEATS);

            myParentFSM.getAgent().send(reply);

            CitizenAgent.logger.debug("NO_SEATS: {} driver REJECTED proposal from {}",
                    myAgent.getLocalName(),
                    offer.message.getSender().getLocalName());

        }


        //TODO:  отменить сценарий пассажира
        myParentFSM.myCitizenAgent.offersPool.clear();
       // myParentFSM.myCitizenAgent.removeBehaviour(myParentFSM.myCitizenAgent.);
        //myParentFSM.myCitizenAgent.best_offer.clear();

        myParentFSM.myCitizenAgent.deregister();
        myParentFSM.myCitizenAgent.removeBehaviour(myParentFSM.myCitizenAgent.myDriverBehaviour);

    }
}
