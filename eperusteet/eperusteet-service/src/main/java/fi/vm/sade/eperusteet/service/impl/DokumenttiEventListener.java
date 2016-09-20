package fi.vm.sade.eperusteet.service.impl;

import org.apache.fop.events.Event;
import org.apache.fop.events.EventFormatter;
import org.apache.fop.events.EventListener;
import org.apache.fop.events.model.EventSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author isaul
 */
public class DokumenttiEventListener implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiEventListener.class);

    @Override
    public void processEvent(Event event) {
        String msg = EventFormatter.format(event);
        EventSeverity severity = event.getSeverity();
        if (severity == EventSeverity.INFO) {
            LOG.info(msg);
        } else if (severity == EventSeverity.WARN) {
            LOG.warn(msg);
        } else if (severity == EventSeverity.ERROR) {
            LOG.error(msg);
        } else if (severity == EventSeverity.FATAL) {
            LOG.error(msg);
        } else {
            assert false;
        }
    }
}
