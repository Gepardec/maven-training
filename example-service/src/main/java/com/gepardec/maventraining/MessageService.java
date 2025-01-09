package com.gepardec.maventraining;

import com.gepardec.maventraining.constants.DomainConstants;

public class MessageService {

    public MessageService() {
    }

    public String getApplicationTitle(){
        return DomainConstants.APP_TITLE;
    }

    public String getApplicationDescription(){
        return DomainConstants.APP_DESCRIPTION;
    }
}
