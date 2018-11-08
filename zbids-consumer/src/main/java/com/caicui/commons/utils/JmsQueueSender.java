package com.caicui.commons.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.Serializable;

@Component
public class JmsQueueSender {
    @Autowired
    private JmsTemplate jmsTemplate;

    public void setConnectionFactory(ConnectionFactory cf) {
        this.jmsTemplate = new JmsTemplate(cf);
    }

    public void simpleSend(final String text) {
        this.jmsTemplate.send("vedu-subject", new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(text);
            }
        });
    }

    public void sendMessage(final Serializable obj) {
        jmsTemplate.send("vedu-subject", new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                ObjectMessage objMessage = session.createObjectMessage(obj);
                return objMessage;
            }
        });
    }

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

}
