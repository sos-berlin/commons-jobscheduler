package com.sos.auth.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sos.auth.rest.permission.model.ObjectFactory;
import com.sos.auth.rest.permission.model.SOSPermissionDashboard;
import com.sos.auth.rest.permission.model.SOSPermissionEvents;
import com.sos.auth.rest.permission.model.SOSPermissionJid;
import com.sos.auth.rest.permission.model.SOSPermissionJoc;
import com.sos.auth.rest.permission.model.SOSPermissionJoe;
import com.sos.auth.rest.permission.model.SOSPermissionRoles;
import com.sos.auth.rest.permission.model.SOSPermissionShiro;
import com.sos.auth.rest.permission.model.SOSPermissionWorkingplan;
import com.sos.auth.rest.permission.model.SOSPermissions;
import com.sos.auth.shiro.SOSlogin;

import org.apache.shiro.session.Session;

@Path("/sosPermission")
public class SOSServicePermissionShiro {

    private SOSShiroCurrentUser currentUser;
    private static SOSShiroCurrentUsersList currentUsersList;
 


    //@Path("/permissions/{name}/{pwd}")

    @GET
    @Path("/permissions")
    @Produces( {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON} )
    public SOSPermissionShiro getPermissions(@QueryParam("session_id")  String sessionId,@QueryParam("user")  String user, @QueryParam("pwd")  String pwd) {
        
       
        if (currentUsersList != null && sessionId != null && sessionId.length() > 0) {
            currentUser = currentUsersList.getUser(sessionId);
        }else {
            if (user != null && user.length() > 0 && pwd != null && pwd.length() > 0){
                currentUser = new SOSShiroCurrentUser(user,pwd);       
                createUser(currentUser);
            }
        }
        
        
        ObjectFactory o = new ObjectFactory();
        SOSPermissionShiro sosPermissionShiro = o.createSOSPermissionShiro();
        
        if (currentUser != null && currentUser.getCurrentSubject() != null){

            
            sosPermissionShiro.setAuthenticated(currentUser.isAuthenticated());
            sosPermissionShiro.setSessionId(currentUser.getSessionId());
            sosPermissionShiro.setUser(currentUser.getUsername());
            
    
            SOSPermissionRoles roles = o.createSOSPermissionRoles();
            addRole(roles.getSOSPermissionRole(), "super");
            addRole(roles.getSOSPermissionRole(), "admin");
            addRole(roles.getSOSPermissionRole(), "joc_admin");
            addRole(roles.getSOSPermissionRole(), "jobeditor");
            addRole(roles.getSOSPermissionRole(), "controller");
            addRole(roles.getSOSPermissionRole(), "workingplan");
            addRole(roles.getSOSPermissionRole(), "jid");
            addRole(roles.getSOSPermissionRole(), "joe");
            addRole(roles.getSOSPermissionRole(), "joc");
            addRole(roles.getSOSPermissionRole(), "events");
    
            SOSPermissions sosPermissions = o.createSOSPermissions();
    
            SOSPermissionJoe sosPermissionJoe = o.createSOSPermissionJoe();
            addPermission(sosPermissionJoe.getSOSPermission(), "sos:products:joe:execute");
            addPermission(sosPermissionJoe.getSOSPermission(), "sos:products:joe:edit");
            addPermission(sosPermissionJoe.getSOSPermission(), "sos:products:joe:write");
            addPermission(sosPermissionJoe.getSOSPermission(), "sos:products:joe:delete");
            sosPermissions.setSOSPermissionJoe(sosPermissionJoe);
    
            SOSPermissionJoc sosPermissionJoc = o.createSOSPermissionJoc();
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:execute");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:start:job");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:add:order");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:add:process_class");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:modify:spooler");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:modify:job");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:modify:job");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:modify:job_chain");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:modify:job_chain_node");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:kill_task"  );
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:add:lock" );
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:remove:lock" );
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:remove:process_class" );
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:remove:job_chain" );
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:remove:order" );
                      
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:terminate");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:view_configuration");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:add_parameter");
            sosPermissions.setSOSPermissionJoc(sosPermissionJoc);
    
            SOSPermissionDashboard sosPermissionDashboard = o.createSOSPermissionDashboard();
            addPermission(sosPermissionDashboard.getSOSPermission(), "sos:products:jid:jobstart");
    
            SOSPermissionEvents sosPermissionEvents = o.createSOSPermissionEvents();
    
   
            SOSPermissionWorkingplan sosPermissionWorkingplan = o.createSOSPermissionWorkingplan();
    
            SOSPermissionJid sosPermissionJid = o.createSOSPermissionJid();
            sosPermissionJid.setSOSPermissionJoc(sosPermissionJoc);
            sosPermissionJid.setSOSPermissionJoe(sosPermissionJoe);
    
            sosPermissionJid.setSOSPermissionJoe(sosPermissionJoe);
            sosPermissionJid.setSOSPermissionJoc(sosPermissionJoc);
            sosPermissionJid.setSOSPermissionDashboard(sosPermissionDashboard);
            sosPermissionJid.setSOSPermissionEvents(sosPermissionEvents);
            sosPermissionJid.setSOSPermissionWorkingplan(sosPermissionWorkingplan);
    
            addPermission(sosPermissionJid.getSOSPermission(), "sos:products:jid:execute");
            addPermission(sosPermissionJid.getSOSPermission(), "sos:products:jid:joetab:show");
            addPermission(sosPermissionJid.getSOSPermission(), "sos:products:jid:joctab:show");
            addPermission(sosPermissionJid.getSOSPermission(), "sos:products:jid:reportstab:show");
            addPermission(sosPermissionJid.getSOSPermission(), "sos:products:jid:eventtab:show");
            addPermission(sosPermissionJid.getSOSPermission(), "sos:products:jid:instances:show");
            addPermission(sosPermissionJid.getSOSPermission(), "sos:products:jid:workingplantab:show");
    
            sosPermissions.setSOSPermissionJid(sosPermissionJid);
    
            sosPermissionShiro.setSOSPermissionRoles(roles);
            sosPermissionShiro.setSOSPermissions(sosPermissions);
        }
        return sosPermissionShiro;

    }

    private void createUser(SOSShiroCurrentUser sosShiroCurrentUser) {
        if (currentUsersList == null) {
            currentUsersList = new SOSShiroCurrentUsersList();
        }
        
        SOSlogin sosLogin = new SOSlogin();
        sosLogin.login(sosShiroCurrentUser.getUsername(), sosShiroCurrentUser.getPassword());
        
        currentUser.setCurrentSubject(sosLogin.getCurrentUser());
        
       
        Session session = sosLogin.getCurrentUser().getSession();
        String sessionId = session.getId().toString();

        currentUser.setSessionId(sessionId);
        currentUsersList.addUser(currentUser);
        
    }
     

    @GET
    @Path("/authenticate")
    @Produces( {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SOSShiroCurrentUserAnswer authenticate(@QueryParam("user") String user, @QueryParam("pwd") String pwd) {
        
        currentUser = new SOSShiroCurrentUser(user,pwd);       
        createUser(currentUser);
        
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(user);
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.getCurrentSubject().isAuthenticated());
        sosShiroCurrentUserAnswer.setSessionId(currentUser.getSessionId());
        sosShiroCurrentUserAnswer.setUser(user);
        
        return sosShiroCurrentUserAnswer;

    }

    @GET
    @Path("/logout")
    @Produces( {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SOSShiroCurrentUserAnswer logout(@QueryParam("session_id") String sessionId) {
        
        currentUser = currentUsersList.getUser(sessionId);
        
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.isAuthenticated());
        sosShiroCurrentUserAnswer.setSessionId(currentUser.getSessionId());
         
        currentUsersList.removeUser(sessionId);

        return sosShiroCurrentUserAnswer;

    }
    
    @GET
    @Path("/role")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SOSShiroCurrentUserAnswer hasRole(@QueryParam("session_id") String sessionId,@QueryParam("user") String user, @QueryParam("pwd") String pwd, @QueryParam("role") String role) {

        if (currentUsersList != null && sessionId != null && sessionId.length() > 0) {
            currentUser = currentUsersList.getUser(sessionId);
        }else {
            if (user != null && user.length() > 0 && pwd != null && pwd.length() > 0){
                currentUser = new SOSShiroCurrentUser(user,pwd);       
                createUser(currentUser);
            }
        }
        

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(user);
        sosShiroCurrentUserAnswer.setRole(role);
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.isAuthenticated());
        sosShiroCurrentUserAnswer.setHasRole(currentUser.hasRole(role));
        
        sosShiroCurrentUserAnswer.setSessionId(currentUser.getSessionId());   
        
        return sosShiroCurrentUserAnswer;

    }
    
    @GET
    @Path("/permission")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SOSShiroCurrentUserAnswer isPermitted(@QueryParam("session_id") String sessionId,@QueryParam("user") String user, @QueryParam("pwd") String pwd, @QueryParam("permission") String permission) {

        if (currentUsersList != null && sessionId != null && sessionId.length() > 0) {
            currentUser = currentUsersList.getUser(sessionId);
        }else {
            if (user != null && user.length() > 0 && pwd != null && pwd.length() > 0){
                currentUser = new SOSShiroCurrentUser(user,pwd);       
                createUser(currentUser);
            }
        }
        
 
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(user);
        sosShiroCurrentUserAnswer.setPermission(permission);
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.isAuthenticated());
        sosShiroCurrentUserAnswer.setIsPermitted(currentUser.isPermitted(permission));

        sosShiroCurrentUserAnswer.setSessionId(currentUser.getSessionId());   

 
        return sosShiroCurrentUserAnswer;

    }     
    
    
    
    private void addPermission(List<String> sosPermission, String permission) {
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