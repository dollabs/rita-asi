import bs4  # BeautifulSoup
import requests

ONE_PAGE = '' # https://www.wikihow.com/Change-the-Battery-on-a-Samsung-Galaxy'
KEYWORD = 'how to make food'
PAGE_LIMIT = 1
MANY_PAGES = 'https://www.wikihow.com/Main-Page'
IGNORE_LINK = "https://www.wikihow.com/feed.rss"

def scrap_problems(url):
    print(url)
    page = requests.get(url)
    soup = bs4.BeautifulSoup(page.text, "html.parser")
    lines = soup.find_all('a')
    links = []
    for line in lines:
        url = str(line.get('href'))
        print("........."+url)
        if "//www.wikihow.com/" in url and url != IGNORE_LINK:
            links.append(url)
    # lines = content.splitlines()
    print( "..." + str(len(links)) + " urls found")
    return links

def scrap_recipe(url):
    page = requests.get(url)
    soup = bs4.BeautifulSoup(page.text, "html.parser")
    content = soup.find(id="bodycontents").get_text()
    lines = content.splitlines()
    return lines

def create_main_urls(keyword):
    url_base = 'https://www.wikihow.com/wikiHowTo?search=' + keyword.replace(" ", "+")
    url_page = url_base + '&start='
    urls = []
    urls.append(url_base)
    # print("Main url: "+url_base)
    for i in range(PAGE_LIMIT-1):
        # print("Main url: "+url_page+str(10*(i+1)))
        urls.append(url_page+str(10*(i+1)))
    print(str(len(urls)) + " main urls found!")
    return urls

if __name__ == '__main__':
    output_folder = 'WikiHow_input/'

    ### Step 1 --- Prepare urls
    if len(ONE_PAGE) == 0:
        urls = []

        # Usage 1 - for all wiki articles in one page with url MANY_PAGES
        if len(KEYWORD) == 0:
            url_main = MANY_PAGES
            urls = scrap_problems(url_main)

        # Usage 2 - for wiki articles related to search keyword KEYWORD, PAGE_LIMIT number of pages
        else:
            for url_main in create_main_urls(KEYWORD):
                for url in scrap_problems(url_main):
                    urls.append(url)

        # Usage 3 - for one wiki page with url ONE_PAGE
    else:
        urls = []
        urls.append(ONE_PAGE)

    ### Step 2 --- Scrap each url on the main page
    for url in urls:
        print("Now scrapping..." + url)
        elements = scrap_recipe(url)
        filename = output_folder + '%s.txt' % url.split("/")[-1]
        problem = url.split("/")[-1].replace("-"," ")
        toPrint = False
        with open(filename, 'w') as f:
            f.write(problem + "\n\n")
            i = 1
            for element in elements:

                # start
                if "Steps" == element:
                    toPrint = True

                # end
                if toPrint and "Community Q&A" == element:
                    break

                # print
                if (
                    toPrint
                    and "')" not in element
                    and "//" not in element
                    and not len(element) == 0
                    ):
                    # print(element + "\n")
                    i = i + 1
                    f.write(element + "\n")

            print("Total lines: "+str(i))
