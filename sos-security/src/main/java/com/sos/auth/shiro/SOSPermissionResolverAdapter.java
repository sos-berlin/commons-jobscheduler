package com.sos.auth.shiro;

import java.util.Collection;
import java.util.Collections;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.realm.text.IniRealm;

public class SOSPermissionResolverAdapter implements RolePermissionResolver {
        private LocalIniRealm realm;

        public Collection<Permission> resolvePermissionsInRole(final String roleString) {
                final SimpleRole role = this.realm.getRole(roleString);
                return role == null ? Collections.<Permission>emptySet() : role.getPermissions();
        }

        public void setIni(final IniRealm ini) {
                this.realm = new LocalIniRealm();
                this.realm.setIni(ini.getIni());
                this.realm.init();
        }

        private static class LocalIniRealm extends IniRealm {
                @Override
                protected SimpleRole getRole(final String rolename) {
                        return super.getRole(rolename);
                }
        }
}