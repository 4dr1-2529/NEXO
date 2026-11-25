package com.estacionnexo.asistencia.model;

public record ResumenAsistencia(long presentes, long completas, long segundosTrabajados) {
    public long horas() {
        return segundosTrabajados / 3600;
    }

    public long minutos() {
        return segundosTrabajados % 3600 / 60;
    }
}
