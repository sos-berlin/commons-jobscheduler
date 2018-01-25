package com.sos.auth.shiro.db;

 
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = "SOS_USER2ROLE")
public class SOSUser2RoleDBItem extends DbItem {

    private Long id;
    private Long roleId;
    private Long userId;
    private SOSUserRoleDBItem sosUserRoleDBItem;
    private SOSUserDBItem sosUserDBItem;

    private List<SOSUserRoleDBItem> sosUserRoleDBItems;

    public SOSUser2RoleDBItem() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "`ID`")
    public Long getId() {
        return id;
    }

    @Column(name = "`ID`")
    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(optional = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "`ROLE_ID`", referencedColumnName = "`ID`", insertable = false, updatable = false)
    public SOSUserRoleDBItem getSosUserRoleDBItem() {
        return sosUserRoleDBItem;
    }

    public void setSosUserRoleDBItem(SOSUserRoleDBItem sosUserRoleDBItem) {
        this.sosUserRoleDBItem = sosUserRoleDBItem;
    }

    @ManyToOne(optional = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "`USER_ID`", referencedColumnName = "`ID`", insertable = false, updatable = false)
    public SOSUserDBItem getSosUserDBItem() {
        return sosUserDBItem;
    }

    public void setSosUserDBItem(SOSUserDBItem sosUserDBItem) {
        this.sosUserDBItem = sosUserDBItem;
    }

    @OneToMany(mappedBy = "id")
    public List<SOSUserRoleDBItem> getSOSUserRoleDBItems() {
        return sosUserRoleDBItems;
    }

    public void setSOSUserRoleDBItems(List<SOSUserRoleDBItem> sosUserRoleDBItems) {
        this.sosUserRoleDBItems = sosUserRoleDBItems;
    }

    @Column(name = "`USER_ID`")
    public Long getUserId() {
        return userId;
    }

    @Column(name = "`USER_ID`")
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Column(name = "`ROLE_ID`")
    public Long getRoleId() {
        return roleId;
    }

    @Column(name = "`ROLE_ID`")
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

}
