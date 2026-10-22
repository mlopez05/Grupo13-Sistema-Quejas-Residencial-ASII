/* PRODUCCIÓN (DOCKER): TODO PASA POR EL MISMO SERVIDOR (NGINX), QUE REENVÍA A CADA SERVICIO */
export const environment = {
  authUrl: '',
  casosUrl: '',
  contenidoUrl: '',
  notificacionesUrl: '',
  bitacoraUrl: '',
  wsUrl: `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/ws`,
};
