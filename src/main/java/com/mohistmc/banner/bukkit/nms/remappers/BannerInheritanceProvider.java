package com.mohistmc.banner.bukkit.nms.remappers;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.mohistmc.banner.bukkit.nms.utils.RemapUtils;
import net.md_5.specialsource.provider.InheritanceProvider;
import org.objectweb.asm.tree.ClassNode;

public class BannerInheritanceProvider implements InheritanceProvider {
    @Override
    public Set<String> getParents(String className) {
        if (className.startsWith("org/springframework/")) {
            return null;
        }
        return fineParents(className, true);
    }

    protected Set<String> fineParents(String className, boolean remap) {
        if (className.startsWith("net/minecraft/")) {
            return fineNMSParents(className, remap);
        } else {
            return findNormalParents(className, remap);
        }
    }

    protected Set<String> fineNMSParents(String className, boolean remap) {
        if (remap) {
            className = RemapUtils.map(className);
        }
        Set<String> parents = new HashSet<>();
        try {
            Class<?> reference = Class.forName(className.replace('/', '.'), false, this.getClass().getClassLoader());
            Class<?> extend = reference.getSuperclass();
            if (extend != null) {
                parents.add(RemapUtils.reverseMap(extend));
            }
            for (Class<?> inter : reference.getInterfaces()) {
                if (inter != null) {
                    parents.add(RemapUtils.reverseMap(inter));
                }
            }
            
            
            String par = "";
            for (String st : parents) {
            	par += st + ", ";
            }
            
            // System.out.println(className + " : " + par);
            return parents;
        } catch (Exception e) {
        	//e.printStackTrace();
            // Empty catch block
        }
        
        String par = "";
        for (String st : parents) {
        	par += st + ", ";
        }
        
        // System.out.println(className + " : " + par);
        return parents;
    }

    protected Set<String> findNormalParents(String className, boolean remap) {
        //TODO: ScriptManager -> java.lang.ArrayIndexOutOfBoundsException: 27210
        ClassNode cn = BannerClassRepo.getInstance().findClass(className);
        if (cn == null) {
            if (!remap) {
                return null;
            }
            String remapClassName = RemapUtils.map(className);
            if (Objects.equals(remapClassName, className)) {
                return null;
            }
            return fineParents(remapClassName, false);
        }
        Set<String> parents = new HashSet<>();
        if (cn.superName != null) {
            parents.add(RemapUtils.reverseMap(cn.superName));
        }
        if (cn.interfaces != null) {
            for (String anInterface : cn.interfaces) {
                parents.add(RemapUtils.reverseMap(anInterface));
            }
        }
        return parents.isEmpty() ? null : parents;
    }
}