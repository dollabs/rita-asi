# Logs file are at:
# /var/log/nginx/webgme.access.log;
# /var/log/nginx/webgme.error.log;
FROM nginx
COPY docker/nginx.conf /etc/nginx/nginx.conf
