/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.persistence;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.extern.log4j.Log4j2;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import javax.persistence.Entity;
import java.util.Properties;
import java.util.stream.Collectors;

@Log4j2
public class SessionFactoryFactoryImpl implements SessionFactoryFactory {

   @Override
    public synchronized SessionFactory create(Properties settings) {

        // set up Hibernate
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(settings).build();
        MetadataSources metadata = new MetadataSources(registry);

        // search for and map Entity classes
        ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .whitelistPackages(settings.getProperty("entity.search.package"))
                .scan();
        ClassInfoList entityClasses = scanResult.getClassesWithAnnotation(Entity.class.getName());
        entityClasses.forEach(classInfo -> metadata.addAnnotatedClass(classInfo.loadClass()));

        // create session factory
        SessionFactory sessionFactory = metadata.buildMetadata().buildSessionFactory();

        // startup log
        String classNames = entityClasses.stream().map(ClassInfo::getSimpleName)
                .collect(Collectors.joining(", "));
        log.info("Persistence mapped classes [{}]", classNames);

        return sessionFactory;
    }
}
