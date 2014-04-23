package com.sos.auth.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sos.auth.rest.permission.model.ObjectFactory;
import com.sos.auth.rest.permission.model.SOSPermissionDashboard;
import com.sos.auth.rest.permission.model.SOSPermissionEvents;
import com.sos.auth.rest.permission.model.SOSPermissionJid;
import com.sos.auth.rest.permission.model.SOSPermissionJobnet;
import com.sos.auth.rest.permission.model.SOSPermissionJoc;
import com.sos.auth.rest.permission.model.SOSPermissionJoe;
import com.sos.auth.rest.permission.model.SOSPermissionRoles;
import com.sos.auth.rest.permission.model.SOSPermissionShiro;
import com.sos.auth.rest.permission.model.SOSPermissionWorkingplan;
import com.sos.auth.rest.permission.model.SOSPermissions;
import com.sos.auth.shiro.SOSlogin;

import org.apache.shiro.subject.Subject;

@Path("/sosPermission")
public class SOSServicePermissionShiro {

    private Subject currentUser;


    //@Path("/permissions/{name}/{pwd}")

    @GET
    @Path("/permissions")
    @Produces( {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON} )
    public SOSPermissionShiro getPermissions(@QueryParam("user")  String user, @QueryParam("pwd")  String pwd) {
        
        SOSlogin sosLogin = new SOSlogin();
        sosLogin.login(user, pwd);

        currentUser = sosLogin.getCurrentUser();
        

        ObjectFactory o = new ObjectFactory();
        SOSPermissionShiro sosPermissionShiro = o.createSOSPermissionShiro();
        
        if (currentUser != null){

            
            sosPermissionShiro.setAuthenticated(currentUser.isAuthenticated());
            sosPermissionShiro.setUser(user);
            
    
            SOSPermissionRoles roles = o.createSOSPermissionRoles();
            addRole(roles.getSOSPermissionRole(), "super");
            addRole(roles.getSOSPermissionRole(), "admin");
            addRole(roles.getSOSPermissionRole(), "jobeditor");
            addRole(roles.getSOSPermissionRole(), "controller");
            addRole(roles.getSOSPermissionRole(), "workingplan");
            addRole(roles.getSOSPermissionRole(), "jid");
            addRole(roles.getSOSPermissionRole(), "joe");
            addRole(roles.getSOSPermissionRole(), "joc");
            addRole(roles.getSOSPermissionRole(), "events");
            addRole(roles.getSOSPermissionRole(), "jobnet");
    
            SOSPermissions sosPermissions = o.createSOSPermissions();
    
            SOSPermissionJoe sosPermissionJoe = o.createSOSPermissionJoe();
            addPermission(sosPermissionJoe.getSOSPermission(), "jobscheduler:joe:execute");
            addPermission(sosPermissionJoe.getSOSPermission(), "jobscheduler:joe:edit");
            addPermission(sosPermissionJoe.getSOSPermission(), "jobscheduler:joe:write");
            addPermission(sosPermissionJoe.getSOSPermission(), "jobscheduler:joe:delete");
            sosPermissions.setSOSPermissionJoe(sosPermissionJoe);
    
            SOSPermissionJoc sosPermissionJoc = o.createSOSPermissionJoc();
            addPermission(sosPermissionJoc.getSOSPermission(), "jobscheduler:joc:execute");
            addPermission(sosPermissionJoc.getSOSPermission(), "jobscheduler:joc:start_job");
            addPermission(sosPermissionJoc.getSOSPermission(), "jobscheduler:joc:start_order");
            addPermission(sosPermissionJoc.getSOSPermission(), "jobscheduler:joc:view_configuration");
            addPermission(sosPermissionJoc.getSOSPermission(), "jobscheduler:joc:shutdown_service");
            addPermission(sosPermissionJoc.getSOSPermission(), "jobscheduler:joc:add_parameter");
            sosPermissions.setSOSPermissionJoc(sosPermissionJoc);
    
            SOSPermissionDashboard sosPermissionDashboard = o.createSOSPermissionDashboard();
            addPermission(sosPermissionDashboard.getSOSPermission(), "jobscheduler:jid:dashboard:start_job");
    
            SOSPermissionEvents sosPermissionEvents = o.createSOSPermissionEvents();
    
            SOSPermissionJobnet sosPermissionJobnet = o.createSOSPermissionJobnet();
    
            SOSPermissionWorkingplan sosPermissionWorkingplan = o.createSOSPermissionWorkingplan();
    
            SOSPermissionJid sosPermissionJid = o.createSOSPermissionJid();
    
            sosPermissionJid.setSOSPermissionJoe(sosPermissionJoe);
            sosPermissionJid.setSOSPermissionJoc(sosPermissionJoc);
            sosPermissionJid.setSOSPermissionDashboard(sosPermissionDashboard);
            sosPermissionJid.setSOSPermissionEvents(sosPermissionEvents);
            sosPermissionJid.setSOSPermissionJobnet(sosPermissionJobnet);
            sosPermissionJid.setSOSPermissionWorkingplan(sosPermissionWorkingplan);
    
            addPermission(sosPermissionJid.getSOSPermission(), "jobscheduler:jid:execute");
            addPermission(sosPermissionJid.getSOSPermission(), "jobscheduler:jid:joetab:show");
            addPermission(sosPermissionJid.getSOSPermission(), "jobscheduler:jid:joctab:show");
            addPermission(sosPermissionJid.getSOSPermission(), "jobscheduler:jid:eventtab:show");
            addPermission(sosPermissionJid.getSOSPermission(), "jobscheduler:jid:jobnettab:show");
            addPermission(sosPermissionJid.getSOSPermission(), "jobscheduler:jid:workingplantab:show");
    
            sosPermissions.setSOSPermissionJid(sosPermissionJid);
    
            sosPermissionShiro.setSOSPermissionRoles(roles);
            sosPermissionShiro.setSOSPermissions(sosPermissions);
        }
        return sosPermissionShiro;

    }

     

    @GET
    @Path("/authenticate")
    @Produces( {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SOSShiroCurrentUserAnswer isAuthenticated(@QueryParam("user") String user, @QueryParam("pwd") String pwd) {

        SOSlogin sosLogin = new SOSlogin();
        sosLogin.login(user, pwd);

        currentUser = sosLogin.getCurrentUser();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(user);
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.isAuthenticated());

        return sosShiroCurrentUserAnswer;

    }

    
    @GET
    @Path("/role")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SOSShiroCurrentUserAnswer hasRole(@QueryParam("user") String user, @QueryParam("pwd") String pwd, @QueryParam("role") String role) {
        SOSlogin sosLogin = new SOSlogin();
        sosLogin.login(user, pwd);

        currentUser = sosLogin.getCurrentUser();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(user);
        sosShiroCurrentUserAnswer.setRole(role);
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.isAuthenticated());
        sosShiroCurrentUserAnswer.setHasRole(currentUser.hasRole(role));


        return sosShiroCurrentUserAnswer;

    }
    
    @GET
    @Path("/permission")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SOSShiroCurrentUserAnswer isPermitted(@QueryParam("user") String user, @QueryParam("pwd") String pwd, @QueryParam("permission") String permission) {
        SOSlogin sosLogin = new SOSlogin();
        sosLogin.login(user, pwd);

        currentUser = sosLogin.getCurrentUser();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(user);
        sosShiroCurrentUserAnswer.setPermission(permission);
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.isAuthenticated());
        sosShiroCurrentUserAnswer.setIsPermitted(currentUser.isPermitted(permission));


        return sosShiroCurrentUserAnswer;

    }     
    
    
    
    private void addPermission(List<String> sosPermission, String permission) {
       /* if (currentUser == null) {
            sosPermission.add("user=null");
        }
        else {
            if (!currentUser.isAuthenticated()) {
                sosPermission.add(String.format("user:%s not authenticated",user));
            }
            else {
                if (!currentUser.isPermitted(permission)) {
                    sosPermission.add(permission + " not allowed");
                }
            }
        }
        */
        if (currentUser != null && currentUser.isPermitted(permission) && currentUser.isAuthenticated()) {
            sosPermission.add(permission);
        }

    }

    private void addRole(List<String> sosRoles, String role) {
        if (currentUser != null && currentUser.hasRole(role) && currentUser.isAuthenticated()) {
            sosRoles.add(role);
        }

    }

}