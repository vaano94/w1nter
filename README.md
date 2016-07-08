3month java junior project
---------
w1nter - это примитивный DI Container по типу Spring Framework.
> Что нужно знать: DI/IoC patterns, Java Core (with extensive Generic Knowledge, Reflection API)

Как это работает?
У вас есть класс Fox
    
    com.animal
    
    public class Fox {
    public int age = 25; 
    }
    
Вешаем на него bean аннотацию

    com.animal
    
    @Snowflake("Mindy")
    public class Fox {
    public int age = 25; 
    }
`@Snowflake(%BEAN_NAME%)` - значит что он помечен как класс объекты которого могут создаваться в специальном контейнере    
  
Что это значит? 

**Инициализируем наш контейнер.**

1. Через конструктор.
    
        Winter winter = new Winter("com.animal");
        
2. Или через метод.

         Winter winter = new Winter();
         winter.addSnowflakes("com.animal");

Обратите внимание, что в обеих методах используется пакет "com.animal" как место,
где будут сканироваться @Snowflake - классы. @Snowflake за пределами не создается.

**Берем теперь нашу лису с этого контейнера.**

    Fox mindy = (Fox) winter.getSnowflake("Mindy");
    System.out.println(mindy.age); // prints 25
    
Вот и все. Почти ;)

**Что еще может w1nter?**

1. Инициализация prototype объекта с помощью дополнительной аннотации `@Copied`
    
        com.animal
        
        @Snowflake("Mindy")
        @Copied
        public class Fox {
        public int age = 25; 
        }
Если будет стоят данная аннотация, то при каждом вызове winter.getSnowflake("") будет создаваться новый объект.

2. Запрет на создание объекта если будет указана аннотация `@Denied`

        com.animal
        
        @Snowflake("Mindy")
        @Denied
        public class Fox {
        public int age = 25; 
        }
        
Если будет стоят данная аннотация, то вы не сможете создать объект через метод `winter.getSnowflake();`

3. Генерация репорта через аннотацию `@Report("C:\someReport")`
        
        com.animal
        @Snowflake("Mindy")
        @Report("C:\someReport")
        public class Fox {
        public int age = 25; 
        }
        
В данном случае после `winter.getSnowflake();` будет создан txt  файл с именем не класса,
а @Snowflake (в данном контексте Mindy) в котором будет информация доступных Reflection API
о всей информации, которая есть в классе (имя классов, доступные аннотации и так далее).        

**При использовании  w1nter нельзя чтобы:**

1. чтобы @Snowflake назывались одинаково - нет двух Mindy. 
При данном происшествии в момент инициализации контейнера должна выпасть соответсвующая ошибка
2. чтобы @Snowflake спокойно создавались если они находились вне пакета который был указан