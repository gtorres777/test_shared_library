FROM  as base

ENV ODOO_USER=odoo

USER ${ODOO_USER}

# Own libraries Odoo
COPY ./repositories/ /opt/odoo_dir/

# Install linux packages for extra Addons
RUN set -x; \
    sudo apt-get -qq update \
    && sudo /dockerfile_extra_addons.sh

# Install python packages for extra Addons
RUN pip3.8 --no-cache-dir install -r /home/odoo/custom-requirements.txt \
    && sudo rm -rf /home/odoo/*

# Own libraries Odoo

CMD ["odoo"]
