/*************************************************************************
 * Copyright 2015 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a
 * source other than Adobe, then your use, modification, or distribution of it requires the prior
 * written permission of Adobe.
 **************************************************************************/
package com.adobe.cq.social.samples.scf.tasks.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;

import com.adobe.cq.social.badging.api.BadgingService;
import com.adobe.cq.social.samples.scf.tasks.api.ProjectSocialComponent;
import com.adobe.cq.social.scf.ClientUtilities;
import com.adobe.cq.social.scf.QueryRequestInfo;
import com.adobe.cq.social.scf.SocialComponent;
import com.adobe.cq.social.scf.SocialComponentFactory;
import com.adobe.cq.social.scf.core.AbstractSocialComponentFactory;
import com.adobe.cq.social.scoring.api.ScoringService;

@Service
@Component
public class ProjectSocialComponentFactory extends AbstractSocialComponentFactory implements SocialComponentFactory {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    protected ResourceResolverFactory resourceResolverFactory;

    @Reference
    private BadgingService badging;

    @Reference
    private ScoringService scoring;

    public SocialComponent getSocialComponent(Resource resource) {
        return new ProjectSocialComponentImpl(resource, getClientUtilities(resource.getResourceResolver()),
            resourceResolverFactory, badging, scoring);
    }

    public SocialComponent getSocialComponent(Resource resource, SlingHttpServletRequest request) {
        return new ProjectSocialComponentImpl(resource, getClientUtilities(request), resourceResolverFactory,
            badging, scoring);
    }

    public SocialComponent getSocialComponent(Resource resource, ClientUtilities clientUtils,
        QueryRequestInfo requestInfo) {
        return new ProjectSocialComponentImpl(resource, clientUtils, resourceResolverFactory, badging, scoring);
    }

    public String getSupportedResourceType() {
        return ProjectSocialComponent.PROJECT_RESOURCE_TYPE;
    }

}
