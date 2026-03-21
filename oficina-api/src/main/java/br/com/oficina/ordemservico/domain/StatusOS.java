package br.com.oficina.ordemservico.domain;

import java.util.Set;

public enum StatusOS {

    RECEBIDA {
        @Override
        public Set<StatusOS> transicoesPermitidas() {
            return Set.of(EM_DIAGNOSTICO);
        }
    },
    EM_DIAGNOSTICO {
        @Override
        public Set<StatusOS> transicoesPermitidas() {
            return Set.of(AGUARDANDO_APROVACAO);
        }
    },
    AGUARDANDO_APROVACAO {
        @Override
        public Set<StatusOS> transicoesPermitidas() {
            return Set.of(EM_EXECUCAO, CANCELADA);
        }
    },
    EM_EXECUCAO {
        @Override
        public Set<StatusOS> transicoesPermitidas() {
            return Set.of(FINALIZADA);
        }
    },
    FINALIZADA {
        @Override
        public Set<StatusOS> transicoesPermitidas() {
            return Set.of(ENTREGUE);
        }
    },
    ENTREGUE {
        @Override
        public Set<StatusOS> transicoesPermitidas() {
            return Set.of();
        }
    },
    CANCELADA {
        @Override
        public Set<StatusOS> transicoesPermitidas() {
            return Set.of();
        }
    };

    public abstract Set<StatusOS> transicoesPermitidas();

    public boolean podeTransicionarPara(StatusOS destino) {
        return transicoesPermitidas().contains(destino);
    }
}
