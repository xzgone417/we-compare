#!/bin/sh

#######################Gradle#######################
checkSdkmanEnv() {
  sdkEnv=$(sdk version)
  sdkVersion="SDKMAN"
  if [[ $gradleInfo =~ ${gradleVersion} ]]
  then
    echo "${sdkVersion} env exist"
  else
    echo "${sdkVersion} not exist"
    installSdkman
  fi
}

installSdkman() {
  curl -s "https://get.sdkman.io" | bash
  source "$HOME/.sdkman/bin/sdkman-init.sh"
  sdk selfupdate force
  sdk version
}

uninstallSdkman() {
  tar zcvf ~/sdkman-backup_$(date +%F-%kh%M).tar.gz -C ~/ .sdkman
  rm -rf ~/.sdkman
}

installGradleBySdkman() {
  # install by sdkman
  checkSdkmanEnv
  sdk install gradle 7.5.1
  gradle -v
}

## 检查 gradle 环境
checkGradleEnv() {
  gradleInfo=$(gradle -v)
  gradleVersion="Gradle 7.5.1"
  if [[ $gradleInfo =~ ${gradleVersion} ]]
  then
    echo "${gradleVersion} env exist"
  else
    echo "${gradleVersion} not exist"
    installGradleBySdkman
  fi
}

#######################NGINX#######################

installNginx() {
  mkdir nginxdir && cd nginxdir
  wget http://nginx.org/download/nginx-1.22.1.tar.gz
  tar -zxvf nginx-1.22.1.tar.gz
  rm nginx-1.22.1.tar.gz
  cd nginx-1.22.1/
  ./configure
  make && make install
  echo "install nginx finish"
}

checkNginxEnv() {
  nginxInfo=$(/usr/local/nginx/sbin/nginx -v)
  nginxVersion="nginx/1.22.1"
  if [[ $nginxInfo =~ ${nginxVersion} ]]
  then
    echo "${nginxVersion} env exist"
  else
    echo "${nginxVersion} not exist"
    installNginx
  fi
}

configProxy() {
  checkNginxEnv
  echo "Check NGINX Env Finish"
}

#######################Grafana#######################

installGrafana() {
  wget https://dl.grafana.com/enterprise/release/grafana-enterprise-9.4.0~beta1-1.x86_64.rpm
  sudo yum install -y grafana-enterprise-9.4.0~beta1-1.x86_64.rpm
  echo "install grafana finish"
}

checkGrafanaEnv() {
  grafanaInfo=$(grafana-server -v)
  grafanaVersion="Version 9.4.0-beta1"
  if [[ $grafanaInfo =~ ${grafanaVersion} ]]
  then
    echo "${grafanaVersion} env exist"
  else
    echo "${grafanaVersion} not exist"
    installGrafana
  fi
}

configGrafana() {
  checkGrafanaEnv
  echo "Check Grafana Env Finish"
}

#######################Config#######################

config() {
  checkGradleEnv
  echo "Check Env Finish"
}

install() {
  checkGradleEnv
  gradle clean
  gradle build
  echo "Install Finish"
}

clear() {
  gradle clean
  echo "Clear Finish"
}

start() {
  checkGradleEnv
  gradle bootRun
}

run() {
  checkGradleEnv
  gradle clean
  gradle build
  gradle bootRun
}

cmd=$1
shift
case $cmd in
  config)
  config "$@"
  ;;
  install)
  install "$@"
  ;;
  start)
  start "$@"
  ;;
  clear)
  clear "$@"
  ;;
  run)
  run "$@"
  ;;
  configProxy)
  configProxy "$@"
  ;;
  configGrafana)
  configGrafana "$@"
esac