package fi.vm.sade.eperusteet.hibernate;

public class MetadataIntegrator {// implements Integrator {
//    private static Map<Class<?>, List<Property>> anywhareReferencedFromProperties = new HashMap<>();
//
//    @Override
//    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
//        metadata.getEntityBindings().forEach(
//                mapping -> Property.mapForClass(mapping.getMappedClass())
//                        .values().stream().filter(p -> p.getContainedTypeOrType()
//                                .isAnnotationPresent(RelatesToPeruste.FromAnywhereReferenced.class)
//                                && !p.isAnnotationPresent(Transient.class))
//                        .forEach(p -> classPropertyList(p.getContainedTypeOrType()).add(p)));
//    }
//
//    private List<Property> classPropertyList(Class<?> clz) {
//        List<Property> list = anywhareReferencedFromProperties.get(clz);
//        if (list == null) {
//            list = new ArrayList<>();
//            anywhareReferencedFromProperties.put(clz, list);
//        }
//        return list;
//    }
//
//    public static List<Property> findPropertiesReferencingTo(Class<?> clz) {
//        List<Property> list = anywhareReferencedFromProperties.get(clz);
//        return list == null ? new ArrayList<>() : list;
//    }
//
//    @Override
//    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
//        anywhareReferencedFromProperties = new HashMap<>();
//    }
}
