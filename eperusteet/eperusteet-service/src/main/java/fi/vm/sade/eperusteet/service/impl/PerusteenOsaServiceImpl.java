package fi.vm.sade.eperusteet.service.impl;

import java.util.List;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author jhyoty
 */
@Service
public class PerusteenOsaServiceImpl implements PerusteenOsaService {

    private static final Logger LOG = LoggerFactory.getLogger(PerusteenOsaServiceImpl.class);
    
    @Autowired
    PerusteenOsaRepository topics;

    @Override
    public List<PerusteenOsa> getAll() {
        return topics.findAll();
    }

    @Override
    public PerusteenOsa get(final Long id) {
        return topics.findOne(id);
    }

    @Override
    public PerusteenOsa add(PerusteenOsa topic) {
        topic.setId(null);
        //topic.setDate(new Date());
        topics.save(topic);
        return topic;
    }

    @Override
    public PerusteenOsa update(final Long id, PerusteenOsa topic) {
        LOG.info("save " + topic);
        topic.setId(id);
        topics.save(topic);
        return topic;
    }

    @Override
    public void delete(final Long id) {
        LOG.info("delete" + id);
        topics.delete(id);
    }

}
