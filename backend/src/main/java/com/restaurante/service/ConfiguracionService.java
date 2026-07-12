package com.restaurante.service;

import com.restaurante.model.Configuracion;
import com.restaurante.repository.ConfiguracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;

    public ConfiguracionService(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    public int getTiempoToleranciaMinutos() {
        return configuracionRepository.findByClave("tiempo_tolerancia_minutos")
                .map(c -> Integer.parseInt(c.getValor()))
                .orElse(30);
    }

    @Transactional
    public void updateTiempoToleranciaMinutos(int minutos) {
        Configuracion config = configuracionRepository.findByClave("tiempo_tolerancia_minutos")
                .orElse(new Configuracion("tiempo_tolerancia_minutos", "30"));
        config.setValor(String.valueOf(minutos));
        configuracionRepository.save(config);
    }

    public int getAlertaIntervaloMinutos() {
        return configuracionRepository.findByClave("alerta_intervalo_minutos")
                .map(c -> Integer.parseInt(c.getValor()))
                .orElse(5);
    }

    @Transactional
    public void updateAlertaIntervaloMinutos(int minutos) {
        Configuracion config = configuracionRepository.findByClave("alerta_intervalo_minutos")
                .orElse(new Configuracion("alerta_intervalo_minutos", "5"));
        config.setValor(String.valueOf(minutos));
        configuracionRepository.save(config);
    }
}
