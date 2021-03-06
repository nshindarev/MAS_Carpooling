package dreamteam.carpooling.appl.PassengerBehaviours;

import dreamteam.carpooling.appl.CitizenAgent;
import dreamteam.carpooling.appl.Util.Conversation;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Calendar;
import java.util.List;

/**
 * Рассылка предложений водителям
 */
public class SendProposalsBehaviour extends OneShotBehaviour {

    private CitizenAgent myCitizenAgent;

    @Override
    public void action() {
        PassengerFSMBehaviour myParentFSM = (PassengerFSMBehaviour) getParent();
        myCitizenAgent = (CitizenAgent) myAgent;

        myParentFSM.currentIterationID = Conversation.getNextID();

        double price = myCitizenAgent.getPrice();
        List<AID> suitableDrivers = myParentFSM.suitableDrivers;
        String id = myParentFSM.currentIterationID;

//        CitizenAgent.logger.info("{} sends PROPOSALS to suitable drivers", myAgent.getLocalName());

        suitableDrivers.forEach(aid -> {
            ACLMessage startConversationMessage = new ACLMessage(ACLMessage.PROPOSE);       // Создаём предложение ...
            startConversationMessage.addReceiver(aid);                                      // ... для текущего водителя. ...
            startConversationMessage.setConversationId(id);                                 // ... Присваиваем ID беседе, ...

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MILLISECOND, Conversation.REPLY_TIME);
            startConversationMessage.setReplyByDate(calendar.getTime());                    // ... устанавливаем срок ответа, ...

            startConversationMessage.setOntology(Conversation.CARPOOLING_ONTOLOGY);
            startConversationMessage.setContent(Conversation.convertProposalDataToContent(  // ... устанавливаем содержимое:
                    myCitizenAgent.getStart(),                                              // - старт,
                    myCitizenAgent.getFinish(),                                             // - финиш,
                    price));                                                                // - цену

            myCitizenAgent.send(startConversationMessage);                                  // Отправляем сообщение

        });
    }
}
