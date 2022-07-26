# Docker compose version of Nzyme

IMPORTANT: the docker is in host network mode. Read https://docs.docker.com/network/host/
- Only working on Linux
- Container’s network stack is not isolated from the Docker host

## Usage

### Download github repository
```
git clone https://github.com/lennartkoopmann/nzyme
cd nzyme/docker
```

### OPTIONAL: Change passwords:
```
bash setRandomPass.sh
```

### Configure files

Please visit the [getting started page](https://www.nzyme.org/docs/intro) to configure nzyme.conf.

- Edit .env file:
    - Set your IP or Domain in EXTERNAL_URL variable
    - OPTIONAL: 
        - Update ADMIN_PASSWORD_HASH (echo -n secretpassword | sha256sum)
        - Change DB config and DATABASE_URL with the same information (DB. user and password)
- Edit nzyme.conf file: 
    - Modify channels in '802_11_monitors'
    - Add APs in '802_11_networks'

### Execute docker-compose in background
```
docker-compose up -d
```

### Show logs 
```
docker-compose logs f
```

## Access webserver

http://IP:22900

## Explanation options

- The config file used is nzyme.conf
- All logs will be in the logs folder. 
- All database data will be in the data folder.
- network_mode: host → Is needed to have access to the host network interfaces inside the container (wlan).
- privileged: true → Needed to have permissions over network interfaces (mode switching).
