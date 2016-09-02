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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.social.badging.api.BadgingService;
import com.adobe.cq.social.badging.api.UserBadge;
import com.adobe.cq.social.samples.scf.tasks.api.ProjectSocialComponent;
import com.adobe.cq.social.scf.ClientUtilities;
import com.adobe.cq.social.scf.User;
import com.adobe.cq.social.scf.core.BaseSocialComponent;
import com.adobe.cq.social.scoring.api.ScoringService;

public class ProjectSocialComponentImpl extends BaseSocialComponent implements ProjectSocialComponent {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectSocialComponentImpl.class);

    private static final String UTILITY_READER = "communities-utility-reader";

    final ValueMap props;
    final BadgingService badging;
    final ScoringService scoring;
    final ResourceResolver resolver;
    final ResourceResolverFactory resourceResolverFactory;

    public ProjectSocialComponentImpl(Resource resource, ClientUtilities clientUtils,
        ResourceResolverFactory resourceResolverFactory, BadgingService badging, ScoringService scoring) {

        super(resource, clientUtils);
        props = resource.adaptTo(ValueMap.class);
        this.badging = badging;
        this.scoring = scoring;
        this.resolver = resource.getResourceResolver();
        this.resourceResolverFactory = resourceResolverFactory;
    }

    @Override
    public String getTitle() {
        return props.get("jcr:title","");
    }

    @Override
    public String getDescription() {
        return props.get("jcr:description","");
    }

    @Override
    public User getOwner() {
        final String ownerId = props.get("userIdentifier","");
        return this.clientUtils.getUser(ownerId, this.resource.getResourceResolver());
    }

    // override this to remove any properties that you don't want showing up in the properties map by default
    @Override
    protected List<String> getIgnoredProperties() {
        this.ignoredProperties.add("jcr:.*");
        this.ignoredProperties.add("owner");
        return this.ignoredProperties;
    }

    @Override
    public List<String> getBadges() {
        List<String> badges = new ArrayList<String>();

        // get the owner of this resource
        final String userid = props.get("userIdentifier", "");

        try {
            // get all of the user's badges
            final List<UserBadge> userBadges =
                badging.getBadges(resource.getResourceResolver(), userid, null, null, BadgingService.ALL_BADGES);

            for (final UserBadge badge : userBadges) {
                badges.add(badge.getImagePath());
            }
        } catch (RepositoryException e) {
            LOG.error("Error calling BadgingService.getBadges() for userid {}", userid, e);
        }

        return badges;
    }

    @Override
    public Long getScore() {
        long score;

        // read the component's properties to get the rule location
        final String ruleLocationProp = clientUtils.getRequest().getResource().getValueMap().get("ruleLocation", "");

        final Resource ruleLocation = resolver.getResource(ruleLocationProp);
        if (ruleLocation == null) {
            LOG.error("Can't read rule location {}", ruleLocationProp);
            return 0L;
        }

        final Resource ruleResource;

        ResourceResolver serviceUserResolver = null;
        try {
            // the rules have restricted read permission, so get a service user to read the resource
            serviceUserResolver =
                resourceResolverFactory.getServiceResourceResolver(Collections.singletonMap(
                    ResourceResolverFactory.SUBSERVICE, (Object) UTILITY_READER));

            // read the component's properties to get the scoring rule
            final String ruleProp = clientUtils.getRequest().getResource().getValueMap().get("scoringRule", "");
            ruleResource = serviceUserResolver.getResource(ruleProp);
            if (ruleResource == null) {
                LOG.error("Can't read rule resource {}", ruleProp);
                return 0L;
            }

            // get the score from the scoring service
            score = scoring.getScore(resolver, props.get("userIdentifier", ""), ruleLocation, ruleResource);
        } catch (LoginException e) {
            LOG.error("Can't get service user.");
            return 0L;
        } catch (RepositoryException e) {
            LOG.error("Error calling ScoringService.getScore() for userid {}", resolver.getUserID(), e);
            return 0L;
        } finally {
            if (serviceUserResolver != null && serviceUserResolver.isLive()) {
                serviceUserResolver.close();
            }
        }

        return score;
    }

}
