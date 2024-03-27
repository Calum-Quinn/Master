### Deploy the elastic load balancer

In this task you will create a load balancer in AWS that will receive
the HTTP requests from clients and forward them to the Drupal
instances.

![Schema](./img/CLD_AWS_INFA.PNG)

## Task 01 Prerequisites for the ELB

* Create a dedicated security group

|Key|Value|
|:--|:--|
|Name|SG-DEVOPSTEAM[XX]-LB|
|Inbound Rules|Application Load Balancer|
|Outbound Rules|Refer to the infra schema|

```bash
[INPUT]

aws ec2 create-security-group --group-name SG-DEVOPSTEAM02-LB --description "Security group for team 2 load balancer" --vpc-id vpc-03d46c285a2af77ba

[OUTPUT]

sg-0979882d4e5089f30

Inbound rules

[INPUT]

aws ec2 authorize-security-group-ingress --group-id sg-0979882d4e5089f30 --protocol tcp --port 8080 --cidr 10.0.0.0/28

[OUTPUT]

True
SECURITYGROUPRULES      10.0.0.0/28     8080    sg-0979882d4e5089f30    709024702237    tcp     False   sgr-08dd0082c202a15bd   8080

```

* Create the Target Group

|Key|Value|
|:--|:--|
|Target type|Instances|
|Name|TG-DEVOPSTEAM[XX]|
|Protocol and port|Refer to the infra schema|
|Ip Address type|IPv4|
|VPC|Refer to the infra schema|
|Protocol version|HTTP1|
|Health check protocol|HTTP|
|Health check path|/|
|Port|Traffic port|
|Healthy threshold|2 consecutive health check successes|
|Unhealthy threshold|2 consecutive health check failures|
|Timeout|5 seconds|
|Interval|10 seconds|
|Success codes|200|

```bash
[INPUT]

// left default value for http code of 200, see doc here: https://docs.aws.amazon.com/cli/latest/reference/elbv2/create-target-group.html
// left default value for --health-check-path of "/", see doc above.

aws elbv2 create-target-group --target-type instance --name TG-DEVOPSTEAM02 --protocol HTTP --port 8080 --ip-address-type ipv4 --vpc-id vpc-03d46c285a2af77ba --protocol-version HTTP1 --health-check-protocol HTTP --health-check-port traffic-port --healthy-threshold-count 2 --unhealthy-threshold-count 2 --health-check-interval-seconds 10 --health-check-timeout-seconds 5

[OUTPUT]

TARGETGROUPS    True    10      /       traffic-port    HTTP    5       2       ipv4    8080    HTTP    HTTP1   arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM02/d6f0f0c87fbf6200    TG-DEVOPSTEAM02 instance        2       vpc-03d46c285a2af77ba
MATCHER 200

[INPUT]

aws elbv2 register-targets --target-group-arn arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM02/d6f0f0c87fbf6200 --targets Id=i-09b1de9e5e3d92e56 Id=i-0a6a5f3b8993a8786

[OUTPUT]

No output, but we have two instances unused targets registered now.
```


## Task 02 Deploy the Load Balancer

[Source](https://aws.amazon.com/elasticloadbalancing/)

* Create the Load Balancer

|Key|Value|
|:--|:--|
|Type|Application Load Balancer|
|Name|ELB-DEVOPSTEAM99|
|Scheme|Internal|
|Ip Address type|IPv4|
|VPC|Refer to the infra schema|
|Security group|Refer to the infra schema|
|Listeners Protocol and port|Refer to the infra schema|
|Target group|Your own target group created in task 01|

Provide the following answers (leave any
field not mentioned at its default value):

```bash
[INPUT]

aws elbv2 create-load-balancer --name ELB-DEVOPSTEAM02 --scheme internal --ip-address-type ipv4 --subnets subnet-0318cbafbe9e9e49a subnet-0de1d6edd623c2dd3 --security-group sg-0979882d4e5089f30

[OUTPUT]

LOADBALANCERS   Z3Q77PNBQS71R4  2024-03-26T08:57:31.180000+00:00        internal-ELB-DEVOPSTEAM02-1681402979.eu-west-3.elb.amazonaws.com        ipv4    arn:aws:elasticloadbalancing:eu-west-3:709024702237:loadbalancer/app/ELB-DEVOPSTEAM02/19b37b562c5e2155      ELB-DEVOPSTEAM02        internal        application     vpc-03d46c285a2af77ba
AVAILABILITYZONES       subnet-0318cbafbe9e9e49a        eu-west-3a
AVAILABILITYZONES       subnet-0de1d6edd623c2dd3        eu-west-3b
SECURITYGROUPS  sg-0979882d4e5089f30
STATE   provisioning

[INPUT]
aws elbv2 register-targets --target-group-arn arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM02/d6f0f0c87fbf6200 --targets Id=i-0a6a5f3b8993a8786 Id=i-09b1de9e5e3d92e56

[INPUT]
aws elbv2 create-listener --load-balancer-arn arn:aws:elasticloadbalancing:eu-west-3:709024702237:loadbalancer/app/ELB-DEVOPSTEAM02/19b37b562c5e2155 --protocol HTTP --port 8080 --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM02/d6f0f0c87fbf6200

[OUTPUT]
LISTENERS       arn:aws:elasticloadbalancing:eu-west-3:709024702237:listener/app/ELB-DEVOPSTEAM02/19b37b562c5e2155/8e5f1cdcbd588e3f     arn:aws:elasticloadbalancing:eu-west-3:709024702237:loadbalancer/app/ELB-DEVOPSTEAM02/19b37b562c5e2155  8080    HTTP
DEFAULTACTIONS  arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM02/d6f0f0c87fbf6200        forward
TARGETGROUPSTICKINESSCONFIG     False
TARGETGROUPS    arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM02/d6f0f0c87fbf6200        1
```

* Get the ELB FQDN (DNS NAME - A Record)

```bash
[INPUT]

 aws elbv2 describe-load-balancers --names "ELB-DEVOPSTEAM02" --region eu-west-3

[OUTPUT]

LOADBALANCERS   Z3Q77PNBQS71R4  2024-03-26T08:57:31.180000+00:00        internal-ELB-DEVOPSTEAM02-1681402979.eu-west-3.elb.amazonaws.com        ipv4    arn:aws:elasticloadbalancing:eu-west-3:709024702237:loadbalancer/app/ELB-DEVOPSTEAM02/19b37b562c5e2155  ELB-DEVOPSTEAM02        internal        application     vpc-03d46c285a2af77ba
AVAILABILITYZONES       subnet-0318cbafbe9e9e49a        eu-west-3a
AVAILABILITYZONES       subnet-0de1d6edd623c2dd3        eu-west-3b
SECURITYGROUPS  sg-0979882d4e5089f30
STATE   active
```

* Get the ELB deployment status

Note : In the EC2 console select the Target Group. In the
       lower half of the panel, click on the **Targets** tab. Watch the
       status of the instance go from **unused** to **initial**.

* Ask the DMZ administrator to register your ELB with the reverse proxy via the private teams channel

* Update your string connection to test your ELB and test it

```bash
//connection string updated

ssh devopsteam02@15.188.43.46 -i CLD_KEY_DMZ_DEVOPSTEAM02.pem -L 2223:10.0.2.10:22 -L 888:10.0.2.10:8080 -L 2224:10.0.2.140:22 -L 889:10.0.2.140:8080 -L 8080:internal-ELB-DEVOPSTEAM02-1681402979.eu-west-3.elb.amazonaws.com:8080
```

* Test your application through your ssh tunneling

```bash
[INPUT]
curl localhost:8080

[OUTPUT]

<!DOCTYPE html>
<html lang="en" dir="ltr" style="--color--primary-hue:202;--color--primary-saturation:79%;--color--primary-lightness:50">
  <head>
    <meta charset="utf-8" />
<meta name="Generator" content="Drupal 10 (https://www.drupal.org)" />
<meta name="MobileOptimized" content="width" />
<meta name="HandheldFriendly" content="true" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<link rel="icon" href="/core/themes/olivero/favicon.ico" type="image/vnd.microsoft.icon" />
<link rel="alternate" type="application/rss+xml" title="" href="http://localhost:8080/rss.xml" />
<link rel="alternate" type="application/rss+xml" title="" href="http://localhost/rss.xml" />

    <title>Welcome! | My blog</title>
    <link rel="stylesheet" media="all" href="/sites/default/files/css/css_Zjlqr4gLlMuyua92biM7g4ysCatyVhbaCA28RKAE6_0.css?delta=0&amp;language=en&amp;theme=olivero&amp;include=eJxdjMEKAyEMBX9ord8U9dUNzZqSuIp_X-jBQi9zmIHx5R1XTOQ4VHjANFbRRBK8L-FWt37rhKGEtEISza8dnkA5BmN6_PJxabnl92s0uFJnbcGRtRWytaODLJ9hcsG_a2Sm8wMVPz8c" />
<link rel="stylesheet" media="all" href="/sites/default/files/css/css_LVcggbffLOYR-ZmHgi5YO9nu66JYLnIs-fYYhz5ctkw.css?delta=1&amp;language=en&amp;theme=olivero&amp;include=eJxdjMEKAyEMBX9ord8U9dUNzZqSuIp_X-jBQi9zmIHx5R1XTOQ4VHjANFbRRBK8L-FWt37rhKGEtEISza8dnkA5BmN6_PJxabnl92s0uFJnbcGRtRWytaODLJ9hcsG_a2Sm8wMVPz8c" />



<link rel="preload" href="/core/themes/olivero/fonts/metropolis/Metropolis-Regular.woff2" as="font" type="font/woff2" crossorigin>
<link rel="preload" href="/core/themes/olivero/fonts/metropolis/Metropolis-SemiBold.woff2" as="font" type="font/woff2" crossorigin>
<link rel="preload" href="/core/themes/olivero/fonts/metropolis/Metropolis-Bold.woff2" as="font" type="font/woff2" crossorigin>
<link rel="preload" href="/core/themes/olivero/fonts/lora/lora-v14-latin-regular.woff2" as="font" type="font/woff2" crossorigin>
    <noscript><link rel="stylesheet" href="/core/themes/olivero/css/components/navigation/nav-primary-no-js.css?s9zj5l" />
</noscript>
  </head>
  <body class="path-frontpage">
        <a href="#main-content" class="visually-hidden focusable skip-link">
      Skip to main content
    </a>

      <div class="dialog-off-canvas-main-canvas" data-off-canvas-main-canvas>

<div id="page-wrapper" class="page-wrapper">
  <div id="page">

          <header id="header" class="site-header" data-drupal-selector="site-header" role="banner">

                <div class="site-header__fixable" data-drupal-selector="site-header-fixable">
          <div class="site-header__initial">
            <button class="sticky-header-toggle" data-drupal-selector="sticky-header-toggle" role="switch" aria-controls="site-header__inner" aria-label="Sticky header" aria-checked="false">
              <span class="sticky-header-toggle__icon">
                <span></span>
                <span></span>
                <span></span>
              </span>
            </button>
          </div>

                    <div id="site-header__inner" class="site-header__inner" data-drupal-selector="site-header-inner">
            <div class="container site-header__inner__container">




<div id="block-olivero-site-branding" class="site-branding block block-system block-system-branding-block">


    <div class="site-branding__inner">
              <div class="site-branding__text">
        <div class="site-branding__name">
          <a href="/" title="Home" rel="home">My blog</a>
        </div>
      </div>
      </div>
</div>

<div class="header-nav-overlay" data-drupal-selector="header-nav-overlay"></div>


                              <div class="mobile-buttons" data-drupal-selector="mobile-buttons">
                  <button class="mobile-nav-button" data-drupal-selector="mobile-nav-button" aria-label="Main Menu" aria-controls="header-nav" aria-expanded="false">
                    <span class="mobile-nav-button__label">Menu</span>
                    <span class="mobile-nav-button__icon"></span>
                  </button>
                </div>

                <div id="header-nav" class="header-nav" data-drupal-selector="header-nav">

<div class="search-block-form block block-search-narrow" data-drupal-selector="search-block-form" id="block-olivero-search-form-narrow" role="search">


      <div class="content">
      <form action="/search/node" method="get" id="search-block-form" accept-charset="UTF-8" class="search-form search-block-form">
  <div class="js-form-item form-item js-form-type-search form-item-keys js-form-item-keys form-no-label">
      <label for="edit-keys" class="form-item__label visually-hidden">Search</label>
        <input title="Enter the terms you wish to search for." placeholder="Search by keyword or phrase." data-drupal-selector="edit-keys" type="search" id="edit-keys" name="keys" value="" size="15" maxlength="128" class="form-search form-element form-element--type-search form-element--api-search" />

        </div>
<div data-drupal-selector="edit-actions" class="form-actions js-form-wrapper form-wrapper" id="edit-actions"><button class="button--primary search-form__submit button js-form-submit form-submit" data-drupal-selector="edit-submit" type="submit" id="edit-submit" value="Search">
    <span class="icon--search"></span>
    <span class="visually-hidden">Search</span>
</button>

</div>

</form>

    </div>
  </div>
<nav  id="block-olivero-main-menu" class="primary-nav block block-menu navigation menu--main" aria-labelledby="block-olivero-main-menu-menu" role="navigation">

  <h2 class="visually-hidden block__title" id="block-olivero-main-menu-menu">Main navigation</h2>






    <ul  class="menu primary-nav__menu primary-nav__menu--level-1" data-drupal-selector="primary-nav-menu--level-1">




        <li class="primary-nav__menu-item primary-nav__menu-item--link primary-nav__menu-item--level-1">

                      <a href="/" class="primary-nav__menu-link primary-nav__menu-link--link primary-nav__menu-link--level-1 is-active" data-drupal-selector="primary-nav-menu-link-has-children" data-drupal-link-system-path="&lt;front&gt;">            <span class="primary-nav__menu-link-inner primary-nav__menu-link-inner--level-1">Home</span>
          </a>


                  </li>
          </ul>



  </nav>




  <div class="region region--secondary-menu">
    <div class="search-block-form block block-search-wide" data-drupal-selector="search-block-form-2" id="block-olivero-search-form-wide" role="search">


      <button class="block-search-wide__button" aria-label="Search Form" data-drupal-selector="block-search-wide-button">
      <svg xmlns="http://www.w3.org/2000/svg" width="22" height="23" viewBox="0 0 22 23">
  <path fill="currentColor" d="M21.7,21.3l-4.4-4.4C19,15.1,20,12.7,20,10c0-5.5-4.5-10-10-10S0,4.5,0,10s4.5,10,10,10c2.1,0,4.1-0.7,5.8-1.8l4.5,4.5c0.4,0.4,1,0.4,1.4,0S22.1,21.7,21.7,21.3z M10,18c-4.4,0-8-3.6-8-8s3.6-8,8-8s8,3.6,8,8S14.4,18,10,18z"/>
</svg>
      <span class="block-search-wide__button-close"></span>
    </button>

        <div class="block-search-wide__wrapper" data-drupal-selector="block-search-wide-wrapper" tabindex="-1">
      <div class="block-search-wide__container">
        <div class="block-search-wide__grid">
          <form action="/search/node" method="get" id="search-block-form--2" accept-charset="UTF-8" class="search-form search-block-form">
  <div class="js-form-item form-item js-form-type-search form-item-keys js-form-item-keys form-no-label">
      <label for="edit-keys--2" class="form-item__label visually-hidden">Search</label>
        <input title="Enter the terms you wish to search for." placeholder="Search by keyword or phrase." data-drupal-selector="edit-keys" type="search" id="edit-keys--2" name="keys" value="" size="15" maxlength="128" class="form-search form-element form-element--type-search form-element--api-search" />

        </div>
<div data-drupal-selector="edit-actions" class="form-actions js-form-wrapper form-wrapper" id="edit-actions--2"><button class="button--primary search-form__submit button js-form-submit form-submit" data-drupal-selector="edit-submit" type="submit" id="edit-submit--2" value="Search">
    <span class="icon--search"></span>
    <span class="visually-hidden">Search</span>
</button>

</div>

</form>

        </div>
      </div>
    </div>
  </div>
<nav  id="block-olivero-account-menu" class="block block-menu navigation menu--account secondary-nav" aria-labelledby="block-olivero-account-menu-menu" role="navigation">

  <span class="visually-hidden" id="block-olivero-account-menu-menu">User account menu</span>




          <ul class="menu secondary-nav__menu secondary-nav__menu--level-1">




        <li class="secondary-nav__menu-item secondary-nav__menu-item--link secondary-nav__menu-item--level-1">
          <a href="/user/login" class="secondary-nav__menu-link secondary-nav__menu-link--link secondary-nav__menu-link--level-1" data-drupal-link-system-path="user/login">Log in</a>

                  </li>
          </ul>



  </nav>

  </div>

                </div>
                          </div>
          </div>
        </div>
      </header>

    <div id="main-wrapper" class="layout-main-wrapper layout-container">
      <div id="main" class="layout-main">
        <div class="main-content">
          <a id="main-content" tabindex="-1"></a>

          <div class="main-content__container container">


  <div class="region region--highlighted grid-full layout--pass--content-medium">
    <div data-drupal-messages-fallback class="hidden messages-list"></div>

  </div>





                          <main role="main">


  <div class="region region--content-above grid-full layout--pass--content-medium">


<div id="block-olivero-page-title" class="block block-core block-page-title-block">



  <h1 class="title page-title">Welcome!</h1>



</div>

  </div>



  <div class="region region--content grid-full layout--pass--content-medium" id="content">


<div id="block-olivero-content" class="block block-system block-system-main-block">


      <div class="block__content">
      <div class="views-element-container">
<div class="view view-frontpage view-id-frontpage view-display-id-page_1 grid-full layout--pass--content-narrow js-view-dom-id-f0a7d3db3052810a9544abf1d94810a779ac7a4a3fd6223d59ea5dd571a63317">





<div class="text-content">
  <p><em>You haven’t created any frontpage content yet.</em></p>
  <h2>Congratulations and welcome to the Drupal community.</h2>
  <p>Drupal is an open source platform for building amazing digital experiences. It’s made, used, taught, documented, and marketed by the <a href="https://www.drupal.org/community">Drupal community</a>. Our community is made up of people from around the world with a shared set of <a href="https://www.drupal.org/about/values-and-principles">values</a>, collaborating together in a respectful manner. As we like to say:</p>
  <blockquote>Come for the code, stay for the community.</blockquote>
  <h2>Get Started</h2>
  <p>There are a few ways to get started with Drupal:</p>
  <ol>
    <li><a href="https://www.drupal.org/docs/user_guide/en/index.html">User Guide:</a> Includes installing, administering, site building, and maintaining the content of a Drupal website.</li>
    <li><a href="/node/add">Create Content:</a> Want to get right to work? Start adding content. <strong>Note:</strong> the information on this page will go away once you add content to your site. Read on and bookmark resources of interest.</li>
    <li><a href="https://www.drupal.org/docs/extending-drupal">Extend Drupal:</a> Drupal’s core software can be extended and customized in remarkable ways. Install additional functionality and change the look of your site using addons contributed by our community.</li>
  </ol>
  <h2>Next Steps</h2>
  <p>Bookmark these links to our active Drupal community groups and support resources.</p>
  <ul>
    <li><a href="https://groups.drupal.org/global-training-days">Global Training Days:</a> Helpful information for evaluating Drupal as a framework and as a career path. Taught in your local language.</li>
    <li><a href="https://www.drupal.org/community/events">Upcoming Events:</a> Learn and connect with others at conferences and events held around the world.</li>
    <li><a href="https://www.drupal.org/community">Community Page:</a> List of key Drupal community groups with their own content.</li>
    <li>Get support and chat with the Drupal community on <a href="https://www.drupal.org/slack">Slack</a> or <a href="https://www.drupal.org/drupalchat">DrupalChat</a>. When you’re looking for a solution to a problem, go to <a href="https://drupal.stackexchange.com/">Drupal Answers on Stack Exchange</a>.</li>
  </ul>
</div>



</div>
</div>

    </div>
  </div>

  </div>

              </main>

          </div>
        </div>
        <div class="social-bar">

<div class="social-bar__inner fixable">
  <div class="rotate">


<div id="block-olivero-syndicate" role="complementary" class="block block-node block-node-syndicate-block">


      <div class="block__content">



<a href="/rss.xml" class="feed-icon">
  <span class="feed-icon__label">
    RSS feed
  </span>
  <span class="feed-icon__icon" aria-hidden="true">
    <svg xmlns="http://www.w3.org/2000/svg" width="14.2" height="14.2" viewBox="0 0 14.2 14.2">
  <path d="M4,12.2c0-2.5-3.9-2.4-3.9,0C0.1,14.7,4,14.6,4,12.2z M9.1,13.4C8.7,9,5.2,5.5,0.8,5.1c-1,0-1,2.7-0.1,2.7c3.1,0.3,5.5,2.7,5.8,5.8c0,0.7,2.1,0.7,2.5,0.3C9.1,13.7,9.1,13.6,9.1,13.4z M14.2,13.5c-0.1-3.5-1.6-6.9-4.1-9.3C7.6,1.7,4.3,0.2,0.8,0c-1,0-1,2.6-0.1,2.6c5.8,0.3,10.5,5,10.8,10.8C11.5,14.5,14.3,14.4,14.2,13.5z"/>
</svg>
  </span>
</a>

    </div>
  </div>

  </div>
</div>

        </div>
      </div>
    </div>

    <footer class="site-footer">
      <div class="site-footer__inner container">



  <div class="region region--footer-bottom grid-full layout--pass--content-medium">


<div id="block-olivero-powered" class="block block-system block-system-powered-by-block">



  <span>
    Powered by    <a href="https://www.drupal.org">Drupal</a>
    <span class="drupal-logo" role="img" aria-label="Drupal Logo">
      <svg width="14" height="19" viewBox="0 0 42.15 55.08" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M29.75 11.73C25.87 7.86 22.18 4.16 21.08 0 20 4.16 16.28 7.86 12.4 11.73 6.59 17.54 0 24.12 0 34a21.08 21.08 0 1042.15 0c0-9.88-6.59-16.46-12.4-22.27zM10.84 35.92a14.13 14.13 0 00-1.65 2.62.54.54 0 01-.36.3h-.18c-.47 0-1-.92-1-.92-.14-.22-.27-.45-.4-.69l-.09-.19C5.94 34.25 7 30.28 7 30.28a17.42 17.42 0 012.52-5.41 31.53 31.53 0 012.28-3l1 1 4.72 4.82a.54.54 0 010 .72l-4.93 5.47zm10.48 13.81a7.29 7.29 0 01-5.4-12.14c1.54-1.83 3.42-3.63 5.46-6 2.42 2.58 4 4.35 5.55 6.29a3.08 3.08 0 01.32.48 7.15 7.15 0 011.3 4.12 7.23 7.23 0 01-7.23 7.25zM35 38.14a.84.84 0 01-.67.58h-.14a1.22 1.22 0 01-.68-.55 37.77 37.77 0 00-4.28-5.31l-1.93-2-6.41-6.65a54 54 0 01-3.84-3.94 1.3 1.3 0 00-.1-.15 3.84 3.84 0 01-.51-1v-.19a3.4 3.4 0 011-3c1.24-1.24 2.49-2.49 3.67-3.79 1.3 1.44 2.69 2.82 4.06 4.19a57.6 57.6 0 017.55 8.58A16 16 0 0135.65 34a14.55 14.55 0 01-.65 4.14z"/>
</svg>
    </span>
  </span>
</div>

  </div>

      </div>
    </footer>

    <div class="overlay" data-drupal-selector="overlay"></div>

  </div>
</div>

  </div>


    <script type="application/json" data-drupal-selector="drupal-settings-json">{"path":{"baseUrl":"\/","pathPrefix":"","currentPath":"node","currentPathIsAdmin":false,"isFront":true,"currentLanguage":"en"},"pluralDelimiter":"\u0003","suppressDeprecationErrors":true,"ajaxTrustedUrl":{"\/search\/node":true},"user":{"uid":0,"permissionsHash":"9e8a75229ea2ed25cd1c7c5bf0f7c2c1d4dc95e0972835f26acb7a078be22d46"}}</script>
<script src="/sites/default/files/js/js_q8oeU2TdRorOB5n67eXJI-UpsbPeKuWdQdF0Y0xI7ZU.js?scope=footer&amp;delta=0&amp;language=en&amp;theme=olivero&amp;include=eJxdjMEKAyEMBX9ord8U9dUNzZqSuIp_X-jBQi9zmIHx5R1XTOQ4VHjANFbRRBK8L-FWt37rhKGEtEISza8dnkA5BmN6_PJxabnl92s0uFJnbcGRtRWytaODLJ9hcsG_a2Sm8wMVPz8c"></script>

  </body>
</html>

```

#### Questions - Analysis

* On your local machine resolve the DNS name of the load balancer into
  an IP address using the `nslookup` command (works on Linux, macOS and Windows). Write
  the DNS name and the resolved IP Address(es) into the report.

```
[INPUT]
nslookup internal-ELB-DEVOPSTEAM02-1681402979.eu-west-3.elb.amazonaws.com

[OUTPUT]
Server:  internetbox.home
Address:  2a02:1210:78f4:8000:a264:8fff:fe6b:82b0

Non-authoritative answer:
Name:    internal-ELB-DEVOPSTEAM02-1681402979.eu-west-3.elb.amazonaws.com
Addresses:  10.0.2.142
          10.0.2.7
```

* From your Drupal instance, identify the ip from which requests are sent by the Load Balancer.

Help : execute `tcpdump port 8080`

```
[INPUT]
tcpdump port 8080

[OUTPUT]
19:16:21.228340 IP provisioner-local.http-alt > 10.0.2.142.32802: Flags [.], ack 131, win 489, options [nop,nop,TS val 42306415 ecr 1826442195], length 0
19:16:26.136359 IP 10.0.2.7.62760 > provisioner-local.http-alt: Flags [S], seq 1114498929, win 26883, options [mss 8961,sackOK,TS val 359146237 ecr 0,nop,wscale 8], length 0
19:16:26.136389 IP provisioner-local.http-alt > 10.0.2.7.62760: Flags [S.], seq 42900061, ack 1114498930, win 62643, options [mss 8961,sackOK,TS val 788800115 ecr 359146237,nop,wscale 7], length 0
19:16:26.136490 IP 10.0.2.7.62760 > provisioner-local.http-alt: Flags [.], ack 1, win 106, options [nop,nop,TS val 359146237 ecr 788800115], length 0
19:16:26.136490 IP 10.0.2.7.62760 > provisioner-local.http-alt: Flags [P.], seq 1:130, ack 1, win 106, options [nop,nop,TS val 359146237 ecr 788800115], length 129: HTTP: GET / HTTP/1.1
19:16:26.136511 IP provisioner-local.http-alt > 10.0.2.7.62760: Flags [.], ack 130, win 489, options [nop,nop,TS val 788800115 ecr 359146237], length 0
19:16:26.144591 IP provisioner-local.http-alt > 10.0.2.7.62760: Flags [P.], seq 1:5618, ack 130, win 489, options [nop,nop,TS val 788800124 ecr 359146237], length 5617: HTTP: HTTP/1.1 200 OK
19:16:26.144659 IP provisioner-local.http-alt > 10.0.2.7.62760: Flags [F.], seq 5618, ack 130, win 489, options [nop,nop,TS val 788800124 ecr 359146237], length 0
19:16:26.144684 IP 10.0.2.7.62760 > provisioner-local.http-alt: Flags [.], ack 5618, win 175, options [nop,nop,TS val 359146245 ecr 788800124], length 0
19:16:26.144737 IP 10.0.2.7.62760 > provisioner-local.http-alt: Flags [F.], seq 130, ack 5618, win 175, options [nop,nop,TS val 359146245 ecr 788800124], length 0
19:16:26.144737 IP 10.0.2.7.62760 > provisioner-local.http-alt: Flags [.], ack 5619, win 175, options [nop,nop,TS val 359146245 ecr 788800124], length 0
19:16:26.144742 IP provisioner-local.http-alt > 10.0.2.7.62760: Flags [.], ack 131, win 489, options [nop,nop,TS val 788800124 ecr 359146245], length 0
```

* In the Apache access log identify the health check accesses from the
  load balancer and copy some samples into the report.

```
[INPUT]
tail /opt/bitnami/apache2/logs/access_log

[OUTPUT]
10.0.2.7 - - [27/Mar/2024:19:17:56 +0000] "GET / HTTP/1.1" 200 5142
10.0.2.142 - - [27/Mar/2024:19:18:01 +0000] "GET / HTTP/1.1" 200 5142
10.0.2.7 - - [27/Mar/2024:19:18:06 +0000] "GET / HTTP/1.1" 200 5142
10.0.2.142 - - [27/Mar/2024:19:18:11 +0000] "GET / HTTP/1.1" 200 5142
10.0.2.7 - - [27/Mar/2024:19:18:16 +0000] "GET / HTTP/1.1" 200 5142
10.0.2.142 - - [27/Mar/2024:19:18:21 +0000] "GET / HTTP/1.1" 200 5142
10.0.2.7 - - [27/Mar/2024:19:18:26 +0000] "GET / HTTP/1.1" 200 5142
10.0.2.142 - - [27/Mar/2024:19:18:31 +0000] "GET / HTTP/1.1" 200 5142
10.0.2.7 - - [27/Mar/2024:19:18:36 +0000] "GET / HTTP/1.1" 200 5142
10.0.2.142 - - [27/Mar/2024:19:18:41 +0000] "GET / HTTP/1.1" 200 5142
```
